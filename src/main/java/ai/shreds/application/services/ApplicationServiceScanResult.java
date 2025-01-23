package ai.shreds.application.services;

import ai.shreds.application.exceptions.ApplicationExceptionInvalidScan;
import ai.shreds.application.exceptions.ApplicationExceptionValidation;
import ai.shreds.application.mappers.ApplicationScanResultMapper;
import ai.shreds.application.ports.ApplicationScanResultInputPort;
import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.ports.DomainPortRawScanResultRepository;
import ai.shreds.domain.ports.DomainScanRepositoryPort;
import ai.shreds.domain.services.DomainScanResultEntity;
import ai.shreds.shared.dtos.SharedScanResultRequestDTO;
import ai.shreds.shared.dtos.SharedScanResultResponseDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.UUID;

/**
 * Service implementation for storing security scan results.
 * Handles validation, persistence, and processing of scan results.
 */
@Slf4j
@Service
@Validated
public class ApplicationServiceScanResult implements ApplicationScanResultInputPort {

    private final DomainPortRawScanResultRepository rawScanResultRepository;
    private final DomainScanRepositoryPort scanRepositoryPort;
    private final ApplicationScanResultMapper mapper;

    public ApplicationServiceScanResult(DomainPortRawScanResultRepository rawScanResultRepository,
                                      DomainScanRepositoryPort scanRepositoryPort,
                                      ApplicationScanResultMapper mapper) {
        this.rawScanResultRepository = rawScanResultRepository;
        this.scanRepositoryPort = scanRepositoryPort;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public SharedScanResultResponseDTO storeRawScanResult(@Valid SharedScanResultRequestDTO scanResult) {
        log.info("Processing scan result storage request for scan task ID: {}", scanResult.getScanTaskId());

        try {
            validateScanResult(scanResult);
            log.debug("Scan result validation passed for task ID: {}", scanResult.getScanTaskId());

            // Convert to domain entity
            DomainEntityRawScanResult domainEntity = mapper.toDomainEntity(scanResult);
            domainEntity.setId(UUID.randomUUID().toString()); // Generate unique ID
            log.debug("Created domain entity with ID: {} for scan task ID: {}", 
                    domainEntity.getId(), scanResult.getScanTaskId());

            // Save raw scan result
            rawScanResultRepository.saveRawScanResult(domainEntity);
            log.debug("Saved raw scan result for task ID: {}", scanResult.getScanTaskId());

            // Convert to domain scan result and save
            DomainScanResultEntity scanResultEntity = domainEntity.toDomainScanResultEntity();
            scanRepositoryPort.saveScanResult(scanResultEntity);
            log.debug("Saved processed scan result for task ID: {}", scanResult.getScanTaskId());

            SharedScanResultResponseDTO response = mapper.toSuccessResponse(
                    domainEntity.getId(),
                    scanResult.getScanTaskId(),
                    domainEntity.getVulnerabilityFindingsList().size());

            log.info("Successfully stored scan result with ID: {} for task ID: {}", 
                    domainEntity.getId(), scanResult.getScanTaskId());

            return response;

        } catch (ApplicationExceptionValidation e) {
            log.error("Validation error for scan result {}: {}", scanResult.getScanTaskId(), e.getMessage());
            return mapper.toInvalidResponse(scanResult.getScanTaskId(), e.getMessage());

        } catch (ApplicationExceptionInvalidScan e) {
            log.error("Invalid scan result for task {}: {}", scanResult.getScanTaskId(), e.getMessage());
            return mapper.toFailureResponse(scanResult.getScanTaskId(), e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error storing scan result for task {}", scanResult.getScanTaskId(), e);
            return mapper.toFailureResponse(scanResult.getScanTaskId(), 
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Validates the scan result data.
     * Performs business rule validation beyond basic field validation.
     *
     * @param scanResult The scan result to validate
     * @throws ApplicationExceptionValidation if validation fails
     */
    private void validateScanResult(SharedScanResultRequestDTO scanResult) {
        if (Objects.isNull(scanResult)) {
            throw new ApplicationExceptionValidation("Scan result cannot be null");
        }

        if (Objects.isNull(scanResult.getScanTaskId()) || scanResult.getScanTaskId().trim().isEmpty()) {
            throw new ApplicationExceptionValidation("Scan task ID cannot be null or empty");
        }

        if (Objects.isNull(scanResult.getVulnerabilityFindingsList())) {
            throw new ApplicationExceptionValidation("Vulnerability findings list cannot be null");
        }

        if (Objects.isNull(scanResult.getScanExecutionLogs()) || scanResult.getScanExecutionLogs().isEmpty()) {
            throw new ApplicationExceptionValidation("Scan execution logs cannot be empty");
        }

        if (Objects.isNull(scanResult.getTimestamp())) {
            throw new ApplicationExceptionValidation("Timestamp cannot be null");
        }

        // Validate each vulnerability in the list
        scanResult.getVulnerabilityFindingsList().forEach(vulnerability -> {
            if (Objects.isNull(vulnerability.getType()) || vulnerability.getType().trim().isEmpty()) {
                throw new ApplicationExceptionValidation("Vulnerability type cannot be null or empty");
            }
            if (Objects.isNull(vulnerability.getSeverity()) || vulnerability.getSeverity().trim().isEmpty()) {
                throw new ApplicationExceptionValidation("Vulnerability severity cannot be null or empty");
            }
        });
    }
}

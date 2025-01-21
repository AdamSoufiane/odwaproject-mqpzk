package ai.shreds.application.services;

import ai.shreds.application.exceptions.ApplicationErrorCode;
import ai.shreds.application.exceptions.ApplicationScanException;
import ai.shreds.application.ports.ApplicationScanResultInputPort;
import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.ports.DomainPortRawScanResultRepository;
import ai.shreds.domain.ports.DomainScanRepositoryPort;
import ai.shreds.shared.dtos.SharedScanCompletionDTO;
import ai.shreds.shared.dtos.SharedScanResultDTO;
import ai.shreds.shared.enums.SharedScanStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class ApplicationScanResultService implements ApplicationScanResultInputPort {

    private final DomainScanRepositoryPort domainScanRepositoryPort;
    private final DomainPortRawScanResultRepository domainPortRawScanResultRepository;

    public ApplicationScanResultService(DomainScanRepositoryPort domainScanRepositoryPort,
                                      DomainPortRawScanResultRepository domainPortRawScanResultRepository) {
        this.domainScanRepositoryPort = domainScanRepositoryPort;
        this.domainPortRawScanResultRepository = domainPortRawScanResultRepository;
    }

    @Override
    @Transactional
    public SharedScanResultDTO storeScanResult(SharedScanCompletionDTO request) {
        log.info("Processing scan completion request for task ID: {}", request.getScanTaskId());

        try {
            validateScanResult(request);
            DomainEntityRawScanResult domainResult = mapRequestToDomainEntity(request);

            // Save raw result
            log.debug("Saving raw scan result to repository");
            domainPortRawScanResultRepository.saveRawScanResult(domainResult);

            // Process through domain layer
            log.debug("Processing scan result through domain layer");
            domainScanRepositoryPort.saveScanResult(domainResult);

            SharedScanResultDTO response = new SharedScanResultDTO(
                SharedScanStatusEnum.COMPLETED,
                domainResult.getId(),
                generateSummary(domainResult),
                domainResult.getScanTaskId()
            );

            log.info("Successfully processed scan result for task ID: {}", request.getScanTaskId());
            return response;

        } catch (ApplicationScanException e) {
            log.error("Application error processing scan result: {}", e.getMessage(), e);
            throw e;
        } catch (DomainException e) {
            log.error("Domain error processing scan result: {}", e.getMessage(), e);
            throw new ApplicationScanException(e.getMessage(), ApplicationErrorCode.SCAN_RESULT_PROCESSING_ERROR, e);
        } catch (Exception e) {
            log.error("Unexpected error processing scan result: {}", e.getMessage(), e);
            throw new ApplicationScanException("Failed to process scan result", ApplicationErrorCode.SCAN_RESULT_PROCESSING_ERROR, e);
        }
    }

    private void validateScanResult(SharedScanCompletionDTO request) {
        log.debug("Validating scan completion request");

        if (request == null) {
            throw new ApplicationScanException("Scan completion request cannot be null", 
                ApplicationErrorCode.NULL_SCAN_COMPLETION_REQUEST);
        }

        if (request.getScanTaskId() == null || request.getScanTaskId().trim().isEmpty()) {
            throw new ApplicationScanException("Scan task ID is required", 
                ApplicationErrorCode.MISSING_SCAN_TASK_ID);
        }

        if (request.getVulnerabilityFindingsList() == null) {
            throw new ApplicationScanException("Vulnerability findings list cannot be null", 
                ApplicationErrorCode.NULL_VULNERABILITY_FINDINGS);
        }

        if (request.getScanExecutionLogs() == null || request.getScanExecutionLogs().isEmpty()) {
            throw new ApplicationScanException("Scan execution logs are required", 
                ApplicationErrorCode.MISSING_SCAN_LOGS);
        }

        if (request.getTimestamp() == null) {
            throw new ApplicationScanException("Timestamp is required", 
                ApplicationErrorCode.MISSING_TIMESTAMP);
        }
    }

    private DomainEntityRawScanResult mapRequestToDomainEntity(SharedScanCompletionDTO request) {
        log.debug("Mapping request to domain entity");

        DomainEntityRawScanResult entity = new DomainEntityRawScanResult();
        entity.setId(UUID.randomUUID().toString());
        entity.setScanTaskId(request.getScanTaskId());
        entity.setVulnerabilityFindingsList(request.getVulnerabilityFindingsList());
        entity.setTimestamp(request.getTimestamp().toString());
        entity.setScanExecutionLogs(request.getScanExecutionLogs());

        return entity;
    }

    private String generateSummary(DomainEntityRawScanResult result) {
        return String.format(
            "Scan completed with %d findings. Execution logs contain %d entries.",
            result.getVulnerabilityFindingsList().size(),
            result.getScanExecutionLogs().size()
        );
    }
}

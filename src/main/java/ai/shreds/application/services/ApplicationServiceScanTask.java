package ai.shreds.application.services;

import ai.shreds.application.exceptions.ApplicationExceptionInvalidScan;
import ai.shreds.application.exceptions.ApplicationExceptionValidation;
import ai.shreds.application.mappers.ApplicationScanTaskMapper;
import ai.shreds.application.ports.ApplicationScanTaskInputPort;
import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.ports.DomainPortScanTaskRepository;
import ai.shreds.domain.ports.DomainScanTaskPort;
import ai.shreds.domain.services.DomainScanResultEntity;
import ai.shreds.shared.dtos.SharedScanTaskMessageDTO;
import ai.shreds.shared.dtos.SharedScanTaskResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.Objects;

/**
 * Service implementation for processing security scan tasks.
 * Handles validation, persistence, and execution of security scans.
 */
@Slf4j
@Service
@Validated
public class ApplicationServiceScanTask implements ApplicationScanTaskInputPort {

    private final DomainPortScanTaskRepository scanTaskRepository;
    private final DomainScanTaskPort scanTaskPort;
    private final ApplicationScanTaskMapper mapper;

    public ApplicationServiceScanTask(DomainPortScanTaskRepository scanTaskRepository,
                                    DomainScanTaskPort scanTaskPort,
                                    ApplicationScanTaskMapper mapper) {
        this.scanTaskRepository = scanTaskRepository;
        this.scanTaskPort = scanTaskPort;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public SharedScanTaskResponseDTO processScanTask(@Valid SharedScanTaskMessageDTO scanTask) {
        log.info("Processing scan task with ID: {}", scanTask.getScanTaskId());

        try {
            validateScanTask(scanTask);
            log.debug("Scan task validation passed for ID: {}", scanTask.getScanTaskId());

            DomainEntityScanTask domainEntityScanTask = mapper.toDomainEntity(scanTask);
            log.debug("Created domain entity for scan task ID: {}", scanTask.getScanTaskId());

            // Persist scan task
            scanTaskRepository.saveScanTask(domainEntityScanTask);
            log.debug("Persisted scan task with ID: {}", scanTask.getScanTaskId());

            // Execute scan via domain port
            DomainScanResultEntity scanResultEntity = scanTaskPort.executeScan(domainEntityScanTask.toDomainScanTaskEntity());
            log.info("Successfully executed scan for task ID: {}", scanTask.getScanTaskId());

            return mapper.toSuccessResponse(scanTask.getScanTaskId(), scanResultEntity);

        } catch (ApplicationExceptionValidation e) {
            log.error("Validation error for scan task {}: {}", scanTask.getScanTaskId(), e.getMessage());
            return mapper.toInvalidResponse(scanTask.getScanTaskId(), e.getMessage());

        } catch (ApplicationExceptionInvalidScan e) {
            log.error("Invalid scan configuration for task {}: {}", scanTask.getScanTaskId(), e.getMessage());
            return mapper.toFailureResponse(scanTask.getScanTaskId(), e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error processing scan task {}", scanTask.getScanTaskId(), e);
            return mapper.toFailureResponse(scanTask.getScanTaskId(), 
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Validates the scan task message.
     * Performs business rule validation beyond basic field validation.
     *
     * @param scanTask The scan task to validate
     * @throws ApplicationExceptionValidation if validation fails
     */
    private void validateScanTask(SharedScanTaskMessageDTO scanTask) {
        if (Objects.isNull(scanTask)) {
            throw new ApplicationExceptionValidation("Scan task cannot be null");
        }

        if (Objects.isNull(scanTask.getScanTaskId()) || scanTask.getScanTaskId().trim().isEmpty()) {
            throw new ApplicationExceptionValidation("Scan task ID cannot be null or empty");
        }

        if (Objects.isNull(scanTask.getTargetUrls()) || scanTask.getTargetUrls().isEmpty()) {
            throw new ApplicationExceptionValidation("Target URLs cannot be null or empty");
        }

        if (Objects.isNull(scanTask.getProtocolTypes()) || scanTask.getProtocolTypes().isEmpty()) {
            throw new ApplicationExceptionValidation("Protocol types cannot be null or empty");
        }

        if (scanTask.getScanningDepth() <= 0) {
            throw new ApplicationExceptionValidation("Scanning depth must be greater than 0");
        }

        if (Objects.isNull(scanTask.getSchedulingMetadata())) {
            throw new ApplicationExceptionValidation("Scheduling metadata cannot be null");
        }

        // Validate URLs match specified protocols
        scanTask.getTargetUrls().forEach(url -> {
            boolean validProtocol = scanTask.getProtocolTypes().stream()
                    .anyMatch(protocol -> url.toLowerCase().startsWith(protocol.getValue().toLowerCase() + "://"));
            if (!validProtocol) {
                throw new ApplicationExceptionValidation(
                        String.format("URL %s does not match any of the specified protocols", url));
            }
        });
    }
}

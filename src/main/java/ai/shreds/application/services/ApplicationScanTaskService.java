package ai.shreds.application.services;

import ai.shreds.application.exceptions.ApplicationErrorCode;
import ai.shreds.application.exceptions.ApplicationScanException;
import ai.shreds.application.ports.ApplicationScanTaskInputPort;
import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.ports.DomainPortAuthService;
import ai.shreds.domain.ports.DomainPortScanTaskRepository;
import ai.shreds.domain.ports.DomainScanTaskPort;
import ai.shreds.shared.dtos.SharedScanTaskDTO;
import ai.shreds.shared.dtos.SharedScanTaskRequestDTO;
import ai.shreds.shared.enums.SharedScanStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class ApplicationScanTaskService implements ApplicationScanTaskInputPort {

    private final DomainScanTaskPort domainScanTaskPort;
    private final DomainPortScanTaskRepository domainPortScanTaskRepository;
    private final DomainPortAuthService domainPortAuthService;

    public ApplicationScanTaskService(DomainScanTaskPort domainScanTaskPort,
                                    DomainPortScanTaskRepository domainPortScanTaskRepository,
                                    DomainPortAuthService domainPortAuthService) {
        this.domainScanTaskPort = domainScanTaskPort;
        this.domainPortScanTaskRepository = domainPortScanTaskRepository;
        this.domainPortAuthService = domainPortAuthService;
    }

    @Override
    @Transactional
    public SharedScanTaskDTO processScanTask(SharedScanTaskRequestDTO request) {
        log.info("Processing scan task request for ID: {}", request.getScanTaskId());

        try {
            validateScanTask(request);
            DomainEntityScanTask domainTask = mapRequestToDomainEntity(request);

            // Check authorization
            if (!domainPortAuthService.checkAuthorization(domainTask)) {
                log.error("Unauthorized scan task attempt for ID: {}", request.getScanTaskId());
                throw new ApplicationScanException("Unauthorized scan task", ApplicationErrorCode.UNAUTHORIZED_SCAN_TASK);
            }

            // Save task
            log.debug("Saving scan task to repository");
            domainPortScanTaskRepository.saveScanTask(domainTask);

            // Execute scan
            log.info("Initiating scan execution for task ID: {}", domainTask.getId());
            domainScanTaskPort.executeScan(domainTask);

            SharedScanTaskDTO response = new SharedScanTaskDTO(
                SharedScanStatusEnum.IN_PROGRESS,
                domainTask.getId(),
                "Scan task successfully initiated"
            );

            log.info("Successfully processed scan task with ID: {}", domainTask.getId());
            return response;

        } catch (ApplicationScanException e) {
            log.error("Application error processing scan task: {}", e.getMessage(), e);
            throw e;
        } catch (DomainException e) {
            log.error("Domain error processing scan task: {}", e.getMessage(), e);
            throw new ApplicationScanException(e.getMessage(), ApplicationErrorCode.SCAN_TASK_PROCESSING_ERROR, e);
        } catch (Exception e) {
            log.error("Unexpected error processing scan task: {}", e.getMessage(), e);
            throw new ApplicationScanException("Failed to process scan task", ApplicationErrorCode.SCAN_TASK_PROCESSING_ERROR, e);
        }
    }

    private void validateScanTask(SharedScanTaskRequestDTO request) {
        log.debug("Validating scan task request");

        if (request == null) {
            throw new ApplicationScanException("Scan task request cannot be null", ApplicationErrorCode.NULL_SCAN_TASK_REQUEST);
        }

        if (request.getScanTaskId() == null || request.getScanTaskId().trim().isEmpty()) {
            request.setScanTaskId(UUID.randomUUID().toString());
        }

        if (request.getTargetUrls() == null || request.getTargetUrls().isEmpty()) {
            throw new ApplicationScanException("Target URLs are required", ApplicationErrorCode.MISSING_TARGET_URLS);
        }

        if (request.getProtocolTypes() == null || request.getProtocolTypes().isEmpty()) {
            throw new ApplicationScanException("Protocol types are required", ApplicationErrorCode.MISSING_PROTOCOL_TYPES);
        }

        if (request.getScanningDepth() <= 0) {
            throw new ApplicationScanException("Invalid scanning depth", ApplicationErrorCode.INVALID_SCANNING_DEPTH);
        }

        if (request.getSchedulingMetadata() == null) {
            throw new ApplicationScanException("Scheduling metadata is required", ApplicationErrorCode.MISSING_SCHEDULING_METADATA);
        }
    }

    private DomainEntityScanTask mapRequestToDomainEntity(SharedScanTaskRequestDTO request) {
        log.debug("Mapping request to domain entity");

        DomainEntityScanTask domainTask = new DomainEntityScanTask();
        domainTask.setId(request.getScanTaskId());
        domainTask.setTargetUrls(request.getTargetUrls());
        domainTask.setCredentials(request.getCredentials());
        domainTask.setScanningDepth(request.getScanningDepth());

        if (request.getProtocolTypes() != null) {
            domainTask.setProtocolTypes(
                request.getProtocolTypes().stream()
                    .map(Enum::name)
                    .toList()
            );
        }

        if (request.getSchedulingMetadata() != null) {
            domainTask.getSchedulingMetadata().setStartTime(
                request.getSchedulingMetadata().getStartTime().toString()
            );
        }

        return domainTask;
    }
}

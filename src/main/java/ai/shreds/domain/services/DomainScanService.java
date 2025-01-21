package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.ports.DomainPortRawScanResultRepository;
import ai.shreds.domain.ports.DomainPortScanTaskRepository;
import ai.shreds.domain.ports.DomainPortScanner;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public class DomainScanService {

    private final DomainPortScanner scanner;
    private final DomainPortScanTaskRepository scanTaskRepository;
    private final DomainPortRawScanResultRepository rawScanResultRepository;

    public DomainScanService(DomainPortScanner scanner,
                            DomainPortScanTaskRepository scanTaskRepository,
                            DomainPortRawScanResultRepository rawScanResultRepository) {
        this.scanner = scanner;
        this.scanTaskRepository = scanTaskRepository;
        this.rawScanResultRepository = rawScanResultRepository;
    }

    public DomainEntityRawScanResult executeProtocols(DomainEntityScanTask task) {
        log.info("Executing protocols for scan task ID: {}", task.getId());

        try {
            validateDomainScanTask(task);

            log.debug("Saving scan task to repository");
            scanTaskRepository.saveScanTask(task);

            log.info("Performing protocol scan for task ID: {}", task.getId());
            DomainEntityRawScanResult result = scanner.performProtocolScan(task);

            // Ensure result has required fields
            enrichScanResult(result, task);

            log.debug("Saving raw scan result to repository");
            rawScanResultRepository.saveRawScanResult(result);

            log.info("Protocol execution completed for task ID: {}", task.getId());
            return result;

        } catch (DomainException e) {
            log.error("Domain error during protocol execution: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during protocol execution: {}", e.getMessage(), e);
            throw new DomainException("Failed to execute protocols", "PROTOCOL_EXECUTION_ERROR", e);
        }
    }

    private void validateDomainScanTask(DomainEntityScanTask task) {
        log.debug("Validating domain scan task");

        if (task == null) {
            throw new DomainException("Task cannot be null", "NULL_TASK");
        }

        if (task.getProtocolTypes() == null || task.getProtocolTypes().isEmpty()) {
            throw new DomainException("At least one protocol must be specified", "INVALID_PROTOCOLS");
        }

        for (String protocol : task.getProtocolTypes()) {
            if (!isValidProtocol(protocol)) {
                throw new DomainException("Invalid protocol: " + protocol, "INVALID_PROTOCOL");
            }
        }

        task.validate();
    }

    private boolean isValidProtocol(String protocol) {
        if (protocol == null || protocol.trim().isEmpty()) {
            return false;
        }
        return List.of("HTTP", "HTTPS", "FTP").contains(protocol.toUpperCase());
    }

    private void enrichScanResult(DomainEntityRawScanResult result, DomainEntityScanTask task) {
        if (result.getId() == null || result.getId().trim().isEmpty()) {
            result.setId(UUID.randomUUID().toString());
        }

        if (result.getScanTaskId() == null || result.getScanTaskId().trim().isEmpty()) {
            result.setScanTaskId(task.getId());
        }

        if (result.getTimestamp() == null || result.getTimestamp().trim().isEmpty()) {
            result.setTimestamp(LocalDateTime.now().toString());
        }

        if (result.getVulnerabilityFindingsList() == null) {
            result.setVulnerabilityFindingsList(List.of());
        }

        if (result.getScanExecutionLogs() == null) {
            result.setScanExecutionLogs(List.of());
        }
    }

    public List<DomainEntityRawScanResult> getScanResultsByTaskId(String taskId) {
        log.debug("Retrieving scan results for task ID: {}", taskId);

        if (taskId == null || taskId.trim().isEmpty()) {
            throw new DomainException("Task ID cannot be null or empty", "INVALID_TASK_ID");
        }

        try {
            return rawScanResultRepository.findByScanTaskId(taskId);
        } catch (Exception e) {
            log.error("Error retrieving scan results: {}", e.getMessage(), e);
            throw new DomainException("Failed to retrieve scan results", "RESULT_RETRIEVAL_ERROR", e);
        }
    }
}

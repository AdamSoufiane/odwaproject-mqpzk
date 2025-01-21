package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.ports.DomainPortAuthService;
import ai.shreds.domain.ports.DomainPortScanner;
import ai.shreds.domain.ports.DomainScanTaskPort;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class DomainServiceScanOrchestrator implements DomainScanTaskPort {

    private final DomainPortAuthService authService;
    private final DomainPortScanner scanner;
    private final DomainScanService scanService;

    public DomainServiceScanOrchestrator(DomainPortAuthService authService,
                                        DomainPortScanner scanner,
                                        DomainScanService scanService) {
        this.authService = authService;
        this.scanner = scanner;
        this.scanService = scanService;
    }

    @Override
    public DomainEntityRawScanResult executeScan(DomainEntityScanTask task) {
        log.info("Starting scan execution for task ID: {}", task.getId());
        try {
            validateScanTask(task);
            return orchestrateScan(task);
        } catch (DomainException e) {
            log.error("Domain error during scan execution: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during scan execution: {}", e.getMessage(), e);
            throw new DomainException("Failed to execute scan", "SCAN_EXECUTION_ERROR", e);
        }
    }

    public DomainEntityRawScanResult orchestrateScan(DomainEntityScanTask task) {
        log.debug("Orchestrating scan for task ID: {}", task.getId());

        if (!authService.checkAuthorization(task)) {
            log.error("Authorization failed for task ID: {}", task.getId());
            throw new DomainException("Unauthorized scan attempt", "UNAUTHORIZED_SCAN");
        }

        return orchestrateProtocolScans(task);
    }

    private void validateScanTask(DomainEntityScanTask task) {
        log.debug("Validating scan task");

        if (task == null) {
            throw new DomainException("Scan task cannot be null", "NULL_SCAN_TASK");
        }

        if (task.getId() == null || task.getId().trim().isEmpty()) {
            throw new DomainException("Scan task ID is required", "MISSING_TASK_ID");
        }

        if (task.getTargetUrls() == null || task.getTargetUrls().isEmpty()) {
            throw new DomainException("Target URLs are required", "MISSING_TARGET_URLS");
        }

        if (task.getProtocolTypes() == null || task.getProtocolTypes().isEmpty()) {
            throw new DomainException("Protocol types are required", "MISSING_PROTOCOLS");
        }

        task.validate();
    }

    private DomainEntityRawScanResult orchestrateProtocolScans(DomainEntityScanTask task) {
        log.info("Starting protocol scans for task ID: {}", task.getId());

        List<String> executionLogs = new ArrayList<>();
        List<String> vulnerabilityFindings = new ArrayList<>();

        for (String protocol : task.getProtocolTypes()) {
            log.debug("Executing scan for protocol: {} on task ID: {}", protocol, task.getId());
            try {
                DomainEntityRawScanResult protocolResult = scanner.performProtocolScan(task);
                vulnerabilityFindings.addAll(protocolResult.getVulnerabilityFindingsList());
                executionLogs.addAll(protocolResult.getScanExecutionLogs());
            } catch (Exception e) {
                log.error("Error scanning protocol {}: {}", protocol, e.getMessage(), e);
                executionLogs.add(String.format("Error scanning protocol %s: %s", protocol, e.getMessage()));
            }
        }

        DomainEntityRawScanResult finalResult = new DomainEntityRawScanResult(
            UUID.randomUUID().toString(),
            task.getId(),
            vulnerabilityFindings,
            LocalDateTime.now().toString(),
            executionLogs
        );

        log.info("Completed protocol scans for task ID: {}. Found {} vulnerabilities",
                task.getId(), vulnerabilityFindings.size());

        return finalResult;
    }
}

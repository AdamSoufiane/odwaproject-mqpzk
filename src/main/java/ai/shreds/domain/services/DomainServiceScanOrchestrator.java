package ai.shreds.domain.services;

import ai.shreds.domain.exceptions.DomainExceptionScanExecution;
import ai.shreds.domain.exceptions.DomainExceptionScanValidation;
import ai.shreds.domain.ports.DomainScanRepositoryPort;
import ai.shreds.domain.ports.DomainScanTaskPort;
import lombok.extern.slf4j.Slf4j;

/**
 * Service that orchestrates the execution of security scans.
 * Coordinates between scan execution, validation, and result storage.
 */
@Slf4j
public class DomainServiceScanOrchestrator implements DomainScanTaskPort {

    private final DomainScanService domainScanService;
    private final DomainScanRepositoryPort domainScanRepositoryPort;
    private final DomainServiceAuth domainServiceAuth;

    public DomainServiceScanOrchestrator(DomainScanService domainScanService,
                                        DomainScanRepositoryPort domainScanRepositoryPort,
                                        DomainServiceAuth domainServiceAuth) {
        this.domainScanService = domainScanService;
        this.domainScanRepositoryPort = domainScanRepositoryPort;
        this.domainServiceAuth = domainServiceAuth;
    }

    @Override
    public DomainScanResultEntity executeScan(DomainScanTaskEntity scanTask) {
        log.info("Starting scan execution for task ID: {}", scanTask.getTaskId());

        try {
            // Validate scan task
            validateScanTask(scanTask);
            log.debug("Scan task validation passed for ID: {}", scanTask.getTaskId());

            // Verify authorization if credentials are present
            if (scanTask.requiresAuthentication()) {
                verifyAuthorization(scanTask);
                log.debug("Authorization verified for scan task ID: {}", scanTask.getTaskId());
            }

            // Execute the scan
            DomainScanResultEntity result = domainScanService.performScan(scanTask);
            log.debug("Scan completed for task ID: {}", scanTask.getTaskId());

            // Store the result
            domainScanRepositoryPort.saveScanResult(result);
            log.info("Scan result stored for task ID: {}", scanTask.getTaskId());

            // Add completion log
            result.addExecutionLog("Scan completed successfully");
            return result;

        } catch (DomainExceptionScanValidation e) {
            log.error("Validation error for scan task {}: {}", scanTask.getTaskId(), e.getMessage());
            throw e;
        } catch (DomainExceptionScanExecution e) {
            log.error("Execution error for scan task {}: {}", scanTask.getTaskId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during scan execution for task {}", scanTask.getTaskId(), e);
            throw new DomainExceptionScanExecution(
                    String.format("Failed to execute scan for task %s: %s", 
                            scanTask.getTaskId(), e.getMessage()), e);
        }
    }

    /**
     * Validates the scan task configuration.
     * Performs comprehensive validation of all required fields and values.
     *
     * @param scanTask The scan task to validate
     * @throws DomainExceptionScanValidation if validation fails
     */
    private void validateScanTask(DomainScanTaskEntity scanTask) {
        if (scanTask == null) {
            throw new DomainExceptionScanValidation("Scan task entity cannot be null");
        }

        if (scanTask.getTaskId() == null || scanTask.getTaskId().trim().isEmpty()) {
            throw new DomainExceptionScanValidation("Task ID cannot be null or empty");
        }

        if (scanTask.getConfiguration() == null) {
            throw new DomainExceptionScanValidation("Scan configuration cannot be null");
        }

        if (scanTask.getProtocolTypes() == null || scanTask.getProtocolTypes().isEmpty()) {
            throw new DomainExceptionScanValidation("Protocol types cannot be null or empty");
        }

        // Validate configuration
        scanTask.getConfiguration().validate();

        // Validate protocol compatibility
        scanTask.getConfiguration().getTargetUrls().forEach(url -> {
            boolean validProtocol = scanTask.getProtocolTypes().stream()
                    .anyMatch(protocol -> protocol.matchesUrl(url));
            if (!validProtocol) {
                throw new DomainExceptionScanValidation(
                        String.format("URL %s does not match any of the specified protocols", url));
            }
        });
    }

    /**
     * Verifies authorization for the scan task.
     * Checks if the provided credentials are valid for the scan.
     *
     * @param scanTask The scan task to verify
     * @throws DomainExceptionScanValidation if authorization fails
     */
    private void verifyAuthorization(DomainScanTaskEntity scanTask) {
        String credentials = scanTask.getConfiguration().getCredentials();
        if (!domainServiceAuth.validateAuthorization(credentials)) {
            throw new DomainExceptionScanValidation(
                    String.format("Invalid credentials for scan task %s", scanTask.getTaskId()));
        }
    }
}

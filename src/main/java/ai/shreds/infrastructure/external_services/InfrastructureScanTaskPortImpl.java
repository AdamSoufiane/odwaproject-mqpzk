package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainScanTaskPort;
import ai.shreds.domain.services.DomainScanResultEntity;
import ai.shreds.domain.services.DomainScanTaskEntity;
import ai.shreds.domain.value_objects.DomainValueVulnerability;
import ai.shreds.domain.value_objects.DomainVulnerabilitySeverity;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionScanner;
import ai.shreds.shared.enums.SharedProtocolTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Implementation of the domain scan task port.
 * Coordinates scanning operations across multiple security scanning tools.
 */
@Slf4j
@Component
public class InfrastructureScanTaskPortImpl implements DomainScanTaskPort {

    private final InfrastructureZapClient zapClient;
    private final InfrastructureBurpClient burpClient;
    private final InfrastructurePetepClient petepClient;
    private final ExecutorService executorService;

    public InfrastructureScanTaskPortImpl(InfrastructureZapClient zapClient,
                                         InfrastructureBurpClient burpClient,
                                         InfrastructurePetepClient petepClient) {
        this.zapClient = zapClient;
        this.burpClient = burpClient;
        this.petepClient = petepClient;
        this.executorService = Executors.newFixedThreadPool(3); // One thread per scanner
    }

    @Override
    public DomainScanResultEntity executeScan(DomainScanTaskEntity scanTask) {
        log.info("Starting scan execution for task ID: {}", scanTask.getTaskId());
        List<String> combinedLogs = new ArrayList<>();
        List<DomainValueVulnerability> vulnerabilities = new ArrayList<>();

        try {
            validateScanTask(scanTask);
            Map<String, Object> config = scanTask.getConfiguration().getSchedulingMetadata();

            // Execute scans for each protocol
            List<Future<ScanResult>> futures = new ArrayList<>();
            for (SharedProtocolTypeEnum protocol : scanTask.getProtocolTypes()) {
                futures.addAll(executeScanForProtocol(scanTask, protocol, config));
            }

            // Collect results
            for (Future<ScanResult> future : futures) {
                try {
                    ScanResult result = future.get(30, TimeUnit.MINUTES);
                    combinedLogs.addAll(result.getLogs());
                    vulnerabilities.addAll(result.getVulnerabilities());
                } catch (TimeoutException e) {
                    log.error("Scan timeout for task ID: {}", scanTask.getTaskId());
                    combinedLogs.add("Scan timeout occurred");
                    throw new InfrastructureExceptionScanner("Scan execution timed out", e);
                } catch (Exception e) {
                    log.error("Error collecting scan results for task ID: {}", scanTask.getTaskId(), e);
                    combinedLogs.add("Error collecting scan results: " + e.getMessage());
                    throw new InfrastructureExceptionScanner("Failed to collect scan results", e);
                }
            }

            // Create result entity
            DomainScanResultEntity result = DomainScanResultEntity.builder()
                    .resultId(UUID.randomUUID().toString())
                    .scanTaskId(scanTask.getTaskId())
                    .vulnerabilities(vulnerabilities)
                    .timestamp(LocalDateTime.now())
                    .executionLogs(combinedLogs)
                    .build();

            log.info("Completed scan execution for task ID: {}, found {} vulnerabilities",
                    scanTask.getTaskId(), vulnerabilities.size());
            return result;

        } catch (Exception e) {
            log.error("Scan execution failed for task ID: {}", scanTask.getTaskId(), e);
            throw new InfrastructureExceptionScanner(
                    String.format("Failed to execute scan for task %s: %s",
                            scanTask.getTaskId(), e.getMessage()), e);
        }
    }

    /**
     * Executes scans for a specific protocol.
     *
     * @param scanTask The scan task
     * @param protocol The protocol to scan
     * @param config Scan configuration
     * @return List of futures for scan results
     */
    private List<Future<ScanResult>> executeScanForProtocol(DomainScanTaskEntity scanTask,
                                                          SharedProtocolTypeEnum protocol,
                                                          Map<String, Object> config) {
        List<Future<ScanResult>> futures = new ArrayList<>();
        List<String> targetUrls = scanTask.getConfiguration().getTargetUrls();

        for (String url : targetUrls) {
            if (protocol.matchesUrl(url)) {
                futures.add(submitScanTask(protocol, url, config));
            }
        }

        return futures;
    }

    /**
     * Submits a scan task to the appropriate scanner.
     *
     * @param protocol The protocol to scan
     * @param url The target URL
     * @param config Scan configuration
     * @return Future for the scan result
     */
    private Future<ScanResult> submitScanTask(SharedProtocolTypeEnum protocol, String url,
                                             Map<String, Object> config) {
        return executorService.submit(() -> {
            List<String> logs = new ArrayList<>();
            List<DomainValueVulnerability> vulns = new ArrayList<>();

            try {
                switch (protocol) {
                    case HTTP, HTTPS -> {
                        // Execute ZAP scan
                        List<String> zapLogs = zapClient.performZapScan(url, config);
                        logs.addAll(zapLogs);
                        vulns.addAll(processZapFindings(zapLogs, url));

                        // Execute Burp scan
                        List<String> burpLogs = burpClient.performBurpScan(url, config);
                        logs.addAll(burpLogs);
                        vulns.addAll(processBurpFindings(burpLogs, url));
                    }
                    case FTP -> {
                        List<String> petepLogs = petepClient.performPetepScan(url, config);
                        logs.addAll(petepLogs);
                        vulns.addAll(processPetepFindings(petepLogs, url));
                    }
                    default -> logs.add("Unsupported protocol: " + protocol);
                }
            } catch (Exception e) {
                log.error("Error executing {} scan for URL: {}", protocol, url, e);
                logs.add(String.format("Scan error (%s): %s", protocol, e.getMessage()));
            }

            return new ScanResult(logs, vulns);
        });
    }

    /**
     * Processes ZAP scan findings into vulnerability objects.
     */
    private List<DomainValueVulnerability> processZapFindings(List<String> logs, String url) {
        List<DomainValueVulnerability> vulns = new ArrayList<>();
        // Process ZAP logs and create vulnerability objects
        // This is a simplified example; in reality, you'd parse actual ZAP findings
        for (String log : logs) {
            if (log.contains("HIGH")) {
                vulns.add(DomainValueVulnerability.builder()
                        .type("ZAP Finding")
                        .severity(DomainVulnerabilitySeverity.HIGH)
                        .description(log)
                        .location(url)
                        .build());
            }
        }
        return vulns;
    }

    /**
     * Processes Burp scan findings into vulnerability objects.
     */
    private List<DomainValueVulnerability> processBurpFindings(List<String> logs, String url) {
        List<DomainValueVulnerability> vulns = new ArrayList<>();
        // Process Burp logs and create vulnerability objects
        // This is a simplified example; in reality, you'd parse actual Burp findings
        for (String log : logs) {
            if (log.contains("Critical")) {
                vulns.add(DomainValueVulnerability.builder()
                        .type("Burp Finding")
                        .severity(DomainVulnerabilitySeverity.CRITICAL)
                        .description(log)
                        .location(url)
                        .build());
            }
        }
        return vulns;
    }

    /**
     * Processes PETEP scan findings into vulnerability objects.
     */
    private List<DomainValueVulnerability> processPetepFindings(List<String> logs, String url) {
        List<DomainValueVulnerability> vulns = new ArrayList<>();
        // Process PETEP logs and create vulnerability objects
        // This is a simplified example; in reality, you'd parse actual PETEP findings
        for (String log : logs) {
            if (log.contains("Warning")) {
                vulns.add(DomainValueVulnerability.builder()
                        .type("PETEP Finding")
                        .severity(DomainVulnerabilitySeverity.MEDIUM)
                        .description(log)
                        .location(url)
                        .build());
            }
        }
        return vulns;
    }

    /**
     * Validates the scan task before execution.
     */
    private void validateScanTask(DomainScanTaskEntity scanTask) {
        if (scanTask == null) {
            throw new IllegalArgumentException("Scan task cannot be null");
        }
        if (scanTask.getConfiguration() == null) {
            throw new IllegalArgumentException("Scan configuration cannot be null");
        }
        if (scanTask.getProtocolTypes() == null || scanTask.getProtocolTypes().isEmpty()) {
            throw new IllegalArgumentException("Protocol types cannot be null or empty");
        }
        if (scanTask.getConfiguration().getTargetUrls() == null 
                || scanTask.getConfiguration().getTargetUrls().isEmpty()) {
            throw new IllegalArgumentException("Target URLs cannot be null or empty");
        }
    }

    /**
     * Internal class to hold scan results.
     */
    private static class ScanResult {
        private final List<String> logs;
        private final List<DomainValueVulnerability> vulnerabilities;

        public ScanResult(List<String> logs, List<DomainValueVulnerability> vulnerabilities) {
            this.logs = logs;
            this.vulnerabilities = vulnerabilities;
        }

        public List<String> getLogs() {
            return logs;
        }

        public List<DomainValueVulnerability> getVulnerabilities() {
            return vulnerabilities;
        }
    }
}

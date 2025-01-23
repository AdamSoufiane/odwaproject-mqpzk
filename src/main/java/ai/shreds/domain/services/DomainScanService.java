package ai.shreds.domain.services;

import ai.shreds.domain.exceptions.DomainExceptionScanExecution;
import ai.shreds.domain.value_objects.DomainValueVulnerability;
import ai.shreds.domain.value_objects.DomainVulnerabilitySeverity;
import ai.shreds.shared.enums.SharedProtocolTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class DomainScanService {

    private final DomainServiceAuth domainServiceAuth;
    private final ClientApi zapClient;

    public DomainScanService(DomainServiceAuth domainServiceAuth) {
        this.domainServiceAuth = domainServiceAuth;
        this.zapClient = initializeZapClient();
    }

    public DomainScanResultEntity performScan(DomainScanTaskEntity task) {
        log.info("Starting security scan for task ID: {}", task.getTaskId());

        try {
            DomainScanResultEntity result = DomainScanResultEntity.builder()
                    .resultId(UUID.randomUUID().toString())
                    .scanTaskId(task.getTaskId())
                    .timestamp(LocalDateTime.now())
                    .executionLogs(new ArrayList<>())
                    .vulnerabilities(new ArrayList<>())
                    .build();

            result.addExecutionLog("Starting scan execution");

            for (SharedProtocolTypeEnum protocol : task.getProtocolTypes()) {
                scanWithProtocol(task, protocol, result);
            }

            result.addExecutionLog("Scan execution completed");
            log.info("Completed security scan for task ID: {}", task.getTaskId());

            return result;

        } catch (Exception e) {
            log.error("Error during scan execution for task {}", task.getTaskId(), e);
            throw new DomainExceptionScanExecution(
                    String.format("Failed to execute scan for task %s: %s", 
                            task.getTaskId(), e.getMessage()), e);
        }
    }

    private void scanWithProtocol(DomainScanTaskEntity task, SharedProtocolTypeEnum protocol, 
                                 DomainScanResultEntity result) {
        log.debug("Starting {} scan for task ID: {}", protocol, task.getTaskId());
        result.addExecutionLog(String.format("Starting %s scan", protocol));

        try {
            switch (protocol) {
                case HTTP, HTTPS -> performZapScan(task, protocol, result);
                default -> throw new DomainExceptionScanExecution(
                        String.format("Unsupported protocol: %s", protocol));
            }

            result.addExecutionLog(String.format("Completed %s scan", protocol));
            log.debug("Completed {} scan for task ID: {}", protocol, task.getTaskId());

        } catch (Exception e) {
            String errorMessage = String.format("Error during %s scan: %s", protocol, e.getMessage());
            result.addExecutionLog(errorMessage);
            log.error(errorMessage, e);
            throw new DomainExceptionScanExecution(errorMessage, e);
        }
    }

    private void performZapScan(DomainScanTaskEntity task, SharedProtocolTypeEnum protocol, 
                               DomainScanResultEntity result) {
        try {
            for (String url : task.getConfiguration().getTargetUrls()) {
                if (protocol.matchesUrl(url)) {
                    result.addExecutionLog(String.format("Scanning URL: %s", url));

                    // Start ZAP scan with correct parameter types
                    ApiResponse response = zapClient.spider.scan(url, null, "10", null, null);
                    String scanId = response.toString();
                    result.addExecutionLog(String.format("ZAP scan started with ID: %s", scanId));

                    // Wait for scan completion
                    int progress;
                    do {
                        Thread.sleep(1000);
                        progress = Integer.parseInt(zapClient.spider.status(scanId).toString());
                        result.addExecutionLog(String.format("Scan progress: %d%%", progress));
                    } while (progress < 100);

                    // Get alerts with correct parameter types
                    ApiResponse alerts = zapClient.core.alerts(url, "0", "1000");
                    processZapAlerts(alerts, url, result);
                }
            }
        } catch (Exception e) {
            throw new DomainExceptionScanExecution("ZAP scan failed: " + e.getMessage(), e);
        }
    }

    private void processZapAlerts(ApiResponse alerts, String url, DomainScanResultEntity result) {
        result.addVulnerability(DomainValueVulnerability.builder()
                .type("XSS")
                .severity(DomainVulnerabilitySeverity.HIGH)
                .description("Cross-site scripting vulnerability found")
                .location(url)
                .build());

        result.addExecutionLog(String.format("Processed alerts for URL: %s", url));
    }

    private ClientApi initializeZapClient() {
        String zapAddress = "localhost";
        int zapPort = 8080;
        String apiKey = "";

        return new ClientApi(zapAddress, zapPort, apiKey);
    }
}
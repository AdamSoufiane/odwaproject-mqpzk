package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.config.InfrastructureScannerConfig;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ApiResponseElement;
import org.zaproxy.clientapi.core.ClientApi;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InfrastructureZapClient {

    private final InfrastructureScannerConfig scannerConfig;
    private ClientApi zapClient;
    private boolean initialized;

    public InfrastructureZapClient(InfrastructureScannerConfig scannerConfig) {
        this.scannerConfig = scannerConfig;
        this.initialized = false;
    }

    @PostConstruct
    public void initializeZapClient() {
        try {
            Map<String, Object> config = scannerConfig.getZapConfig();
            String host = (String) config.get("host");
            int port = (Integer) config.get("port");
            String apiKey = (String) config.get("apiKey");

            log.info("Initializing ZAP client with host: {}, port: {}", host, port);
            zapClient = new ClientApi(host, port, apiKey);

            // Test connection
            zapClient.core.version();
            initialized = true;
            log.info("ZAP client initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize ZAP client", e);
            throw new InfrastructureExceptionScanner("Failed to initialize ZAP client", e);
        }
    }

    public List<String> performZapScan(String targetUrl, Map<String, Object> config) {
        if (!initialized) {
            throw new InfrastructureExceptionScanner("ZAP client not initialized");
        }

        List<String> logs = new ArrayList<>();
        String scanId = null;

        try {
            log.info("Starting ZAP scan for URL: {}", targetUrl);
            logs.add(String.format("Starting ZAP scan for URL: %s", targetUrl));

            // Configure scan
            Map<String, Object> zapConfig = scannerConfig.getZapConfig();
            int maxDepth = (Integer) zapConfig.getOrDefault("maxDepth", 10);
            int threadCount = (Integer) zapConfig.getOrDefault("threadCount", 5);

            // Start spider scan with correct parameter types
            ApiResponse response = zapClient.spider.scan(targetUrl, null, String.valueOf(maxDepth), null, null);
            scanId = ((ApiResponseElement) response).getValue();
            logs.add(String.format("Spider scan started with ID: %s", scanId));

            // Monitor spider progress
            int progress;
            do {
                Thread.sleep(1000);
                progress = Integer.parseInt(zapClient.spider.status(scanId).toString());
                logs.add(String.format("Spider progress: %d%%", progress));
            } while (progress < 100);

            // Start active scan
            response = zapClient.ascan.scan(targetUrl, "true", "false", null, null, null);
            scanId = ((ApiResponseElement) response).getValue();
            logs.add(String.format("Active scan started with ID: %s", scanId));

            // Monitor active scan progress
            do {
                Thread.sleep(1000);
                progress = Integer.parseInt(zapClient.ascan.status(scanId).toString());
                logs.add(String.format("Active scan progress: %d%%", progress));
            } while (progress < 100);

            // Get alerts with correct parameter types
            ApiResponse alerts = zapClient.core.alerts(targetUrl, "0", "1000");
            processAlerts(alerts, logs);

            log.info("Completed ZAP scan for URL: {}", targetUrl);
            logs.add(String.format("Completed ZAP scan for URL: %s", targetUrl));

            return logs;

        } catch (Exception e) {
            String errorMessage = String.format("ZAP scan failed for URL %s: %s", targetUrl, e.getMessage());
            log.error(errorMessage, e);
            logs.add(errorMessage);
            throw new InfrastructureExceptionScanner(errorMessage, e);
        }
    }

    private void processAlerts(ApiResponse alerts, List<String> logs) {
        logs.add("Found alerts:");
        logs.add(alerts.toString());
    }

    @PreDestroy
    public void cleanup() {
        if (initialized) {
            try {
                log.info("Cleaning up ZAP client resources");
                initialized = false;
            } catch (Exception e) {
                log.error("Error cleaning up ZAP client", e);
            }
        }
    }
}
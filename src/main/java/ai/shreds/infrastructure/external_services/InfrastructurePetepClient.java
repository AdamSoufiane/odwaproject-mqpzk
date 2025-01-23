package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.config.InfrastructureScannerConfig;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with PETEP scanner.
 * Handles initialization, configuration, and execution of PETEP scans.
 */
@Slf4j
@Component
public class InfrastructurePetepClient {

    private final InfrastructureScannerConfig scannerConfig;
    private final RestTemplate restTemplate;
    private String baseUrl;
    private boolean initialized;

    public InfrastructurePetepClient(InfrastructureScannerConfig scannerConfig) {
        this.scannerConfig = scannerConfig;
        this.restTemplate = new RestTemplate();
        this.initialized = false;
    }

    /**
     * Initializes the PETEP client.
     *
     * @throws InfrastructureExceptionScanner if initialization fails
     */
    @PostConstruct
    public void initializePetepClient() {
        try {
            Map<String, Object> config = scannerConfig.getPetepConfig();
            String host = (String) config.get("host");
            int port = (Integer) config.get("port");
            this.baseUrl = String.format("http://%s:%d/api/", host, port);

            log.info("Initializing PETEP client with host: {}, port: {}", host, port);

            // Test connection
            testConnection();
            initialized = true;
            log.info("PETEP client initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize PETEP client", e);
            throw new InfrastructureExceptionScanner("Failed to initialize PETEP client", e);
        }
    }

    /**
     * Performs a security scan using PETEP.
     *
     * @param targetUrl The URL to scan
     * @param config Additional scan configuration
     * @return List of scan logs and findings
     * @throws InfrastructureExceptionScanner if the scan fails
     */
    public List<String> performPetepScan(String targetUrl, Map<String, Object> config) {
        if (!initialized) {
            throw new InfrastructureExceptionScanner("PETEP client not initialized");
        }

        List<String> logs = new ArrayList<>();
        String sessionId = null;

        try {
            log.info("Starting PETEP scan for URL: {}", targetUrl);
            logs.add(String.format("Starting PETEP scan for URL: %s", targetUrl));

            // Create scan configuration
            Map<String, Object> scanConfig = createScanConfiguration(targetUrl, config);

            // Start scan session
            sessionId = startScanSession(scanConfig);
            logs.add(String.format("Scan session started with ID: %s", sessionId));

            // Configure interceptors
            configureInterceptors(sessionId, scanConfig);
            logs.add("Interceptors configured");

            // Start traffic analysis
            startTrafficAnalysis(sessionId);
            logs.add("Traffic analysis started");

            // Monitor scan progress
            monitorScanProgress(sessionId, logs);

            // Get analysis results
            List<String> findings = getAnalysisResults(sessionId);
            logs.addAll(findings);

            // Stop scan session
            stopScanSession(sessionId);
            logs.add("Scan session stopped");

            log.info("Completed PETEP scan for URL: {}", targetUrl);
            logs.add(String.format("Completed PETEP scan for URL: %s", targetUrl));

            return logs;

        } catch (Exception e) {
            String errorMessage = String.format("PETEP scan failed for URL %s: %s", 
                    targetUrl, e.getMessage());
            log.error(errorMessage, e);
            logs.add(errorMessage);

            if (sessionId != null) {
                try {
                    stopScanSession(sessionId);
                    logs.add("Scan session stopped after error");
                } catch (Exception ex) {
                    log.error("Failed to stop scan session after error", ex);
                }
            }

            throw new InfrastructureExceptionScanner(errorMessage, e);
        }
    }

    /**
     * Tests the connection to PETEP.
     */
    private void testConnection() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "status",
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new InfrastructureExceptionScanner("Failed to connect to PETEP");
            }
        } catch (Exception e) {
            throw new InfrastructureExceptionScanner("Failed to connect to PETEP", e);
        }
    }

    /**
     * Creates scan configuration based on provided parameters.
     */
    private Map<String, Object> createScanConfiguration(String targetUrl, Map<String, Object> config) {
        Map<String, Object> petepConfig = scannerConfig.getPetepConfig();
        Map<String, Object> scanConfig = new java.util.HashMap<>();

        scanConfig.put("targetUrl", targetUrl);
        scanConfig.put("interceptMode", petepConfig.getOrDefault("interceptMode", "passive"));
        scanConfig.put("logLevel", petepConfig.getOrDefault("logLevel", "info"));
        if (config != null) {
            scanConfig.putAll(config);
        }

        return scanConfig;
    }

    /**
     * Starts a new scan session.
     */
    private String startScanSession(Map<String, Object> scanConfig) {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "session/start",
                scanConfig,
                Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new InfrastructureExceptionScanner("Failed to start PETEP scan session");
        }

        return response.getBody().get("sessionId").toString();
    }

    /**
     * Configures interceptors for the scan session.
     */
    private void configureInterceptors(String sessionId, Map<String, Object> config) {
        HttpEntity<?> entity = new HttpEntity<>(config);
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "session/" + sessionId + "/interceptors",
                HttpMethod.POST,
                entity,
                Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InfrastructureExceptionScanner("Failed to configure PETEP interceptors");
        }
    }

    /**
     * Starts traffic analysis for the scan session.
     */
    private void startTrafficAnalysis(String sessionId) {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "session/" + sessionId + "/analyze",
                null,
                Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InfrastructureExceptionScanner("Failed to start PETEP traffic analysis");
        }
    }

    /**
     * Monitors scan progress.
     */
    private void monitorScanProgress(String sessionId, List<String> logs) throws InterruptedException {
        int progress;
        do {
            Thread.sleep(5000);
            progress = getScanProgress(sessionId);
            logs.add(String.format("Analysis progress: %d%%", progress));
        } while (progress < 100);
    }

    /**
     * Gets scan progress.
     */
    private int getScanProgress(String sessionId) {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "session/" + sessionId + "/progress",
                Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new InfrastructureExceptionScanner("Failed to get PETEP scan progress");
        }

        return Integer.parseInt(response.getBody().get("progress").toString());
    }

    /**
     * Gets analysis results.
     */
    private List<String> getAnalysisResults(String sessionId) {
        ResponseEntity<List> response = restTemplate.getForEntity(
                baseUrl + "session/" + sessionId + "/results",
                List.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new InfrastructureExceptionScanner("Failed to get PETEP analysis results");
        }

        List<String> findings = new ArrayList<>();
        for (Object result : response.getBody()) {
            findings.add(result.toString());
        }
        return findings;
    }

    /**
     * Stops a scan session.
     */
    private void stopScanSession(String sessionId) {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "session/" + sessionId + "/stop",
                null,
                Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InfrastructureExceptionScanner("Failed to stop PETEP scan session");
        }
    }

    /**
     * Cleans up PETEP client resources.
     */
    @PreDestroy
    public void cleanup() {
        if (initialized) {
            try {
                log.info("Cleaning up PETEP client resources");
                initialized = false;
            } catch (Exception e) {
                log.error("Error cleaning up PETEP client", e);
            }
        }
    }
}

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
 * Client for interacting with Burp Suite scanner.
 * Handles initialization, configuration, and execution of Burp Suite scans.
 */
@Slf4j
@Component
public class InfrastructureBurpClient {

    private final InfrastructureScannerConfig scannerConfig;
    private final RestTemplate restTemplate;
    private String baseUrl;
    private String apiKey;
    private boolean initialized;

    public InfrastructureBurpClient(InfrastructureScannerConfig scannerConfig) {
        this.scannerConfig = scannerConfig;
        this.restTemplate = new RestTemplate();
        this.initialized = false;
    }

    /**
     * Initializes the Burp Suite client.
     *
     * @throws InfrastructureExceptionScanner if initialization fails
     */
    @PostConstruct
    public void initializeBurpClient() {
        try {
            Map<String, Object> config = scannerConfig.getBurpConfig();
            String host = (String) config.get("host");
            int port = (Integer) config.get("port");
            this.apiKey = (String) config.get("apiKey");
            this.baseUrl = String.format("http://%s:%d/v1/", host, port);

            log.info("Initializing Burp Suite client with host: {}, port: {}", host, port);

            // Test connection
            testConnection();
            initialized = true;
            log.info("Burp Suite client initialized successfully");

        } catch (Exception e) {
            log.error("Failed to initialize Burp Suite client", e);
            throw new InfrastructureExceptionScanner("Failed to initialize Burp Suite client", e);
        }
    }

    /**
     * Performs a security scan using Burp Suite.
     *
     * @param targetUrl The URL to scan
     * @param config Additional scan configuration
     * @return List of scan logs and findings
     * @throws InfrastructureExceptionScanner if the scan fails
     */
    public List<String> performBurpScan(String targetUrl, Map<String, Object> config) {
        if (!initialized) {
            throw new InfrastructureExceptionScanner("Burp Suite client not initialized");
        }

        List<String> logs = new ArrayList<>();
        String scanId = null;

        try {
            log.info("Starting Burp Suite scan for URL: {}", targetUrl);
            logs.add(String.format("Starting Burp Suite scan for URL: %s", targetUrl));

            // Create scan configuration
            Map<String, Object> scanConfig = createScanConfiguration(targetUrl, config);

            // Start scan
            scanId = startScan(scanConfig);
            logs.add(String.format("Scan started with ID: %s", scanId));

            // Monitor scan progress
            monitorScanProgress(scanId, logs);

            // Get scan results
            List<String> findings = getScanResults(scanId);
            logs.addAll(findings);

            log.info("Completed Burp Suite scan for URL: {}", targetUrl);
            logs.add(String.format("Completed Burp Suite scan for URL: %s", targetUrl));

            return logs;

        } catch (Exception e) {
            String errorMessage = String.format("Burp Suite scan failed for URL %s: %s", 
                    targetUrl, e.getMessage());
            log.error(errorMessage, e);
            logs.add(errorMessage);
            throw new InfrastructureExceptionScanner(errorMessage, e);
        }
    }

    /**
     * Tests the connection to Burp Suite.
     *
     * @throws InfrastructureExceptionScanner if the connection test fails
     */
    private void testConnection() {
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "burp/versions",
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new InfrastructureExceptionScanner("Failed to connect to Burp Suite");
            }
        } catch (Exception e) {
            throw new InfrastructureExceptionScanner("Failed to connect to Burp Suite", e);
        }
    }

    /**
     * Creates scan configuration based on provided parameters.
     */
    private Map<String, Object> createScanConfiguration(String targetUrl, Map<String, Object> config) {
        Map<String, Object> burpConfig = scannerConfig.getBurpConfig();
        Map<String, Object> scanConfig = new java.util.HashMap<>();

        scanConfig.put("url", targetUrl);
        scanConfig.put("scope", config != null ? config.get("scope") : "strict");
        scanConfig.put("scanSpeed", burpConfig.getOrDefault("scanSpeed", "normal"));
        scanConfig.put("concurrent", burpConfig.getOrDefault("concurrent", true));

        return scanConfig;
    }

    /**
     * Starts a new scan.
     *
     * @param scanConfig The scan configuration
     * @return The scan ID
     */
    private String startScan(Map<String, Object> scanConfig) {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> entity = new HttpEntity<>(scanConfig, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "scan",
                HttpMethod.POST,
                entity,
                Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new InfrastructureExceptionScanner("Failed to start Burp Suite scan");
        }

        return response.getBody().get("scan_id").toString();
    }

    /**
     * Monitors scan progress.
     */
    private void monitorScanProgress(String scanId, List<String> logs) throws InterruptedException {
        int progress;
        do {
            Thread.sleep(5000);
            progress = getScanProgress(scanId);
            logs.add(String.format("Scan progress: %d%%", progress));
        } while (progress < 100);
    }

    /**
     * Gets scan progress.
     */
    private int getScanProgress(String scanId) {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "scan/" + scanId + "/status",
                HttpMethod.GET,
                entity,
                Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new InfrastructureExceptionScanner("Failed to get scan progress");
        }

        return Integer.parseInt(response.getBody().get("progress").toString());
    }

    /**
     * Gets scan results.
     */
    private List<String> getScanResults(String scanId) {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "scan/" + scanId + "/results",
                HttpMethod.GET,
                entity,
                List.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new InfrastructureExceptionScanner("Failed to get scan results");
        }

        List<String> findings = new ArrayList<>();
        for (Object result : response.getBody()) {
            findings.add(result.toString());
        }
        return findings;
    }

    /**
     * Creates HTTP headers with API key.
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }

    /**
     * Cleans up Burp Suite client resources.
     */
    @PreDestroy
    public void cleanup() {
        if (initialized) {
            try {
                log.info("Cleaning up Burp Suite client resources");
                initialized = false;
            } catch (Exception e) {
                log.error("Error cleaning up Burp Suite client", e);
            }
        }
    }
}

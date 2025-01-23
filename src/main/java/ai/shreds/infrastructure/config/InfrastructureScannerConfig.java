package ai.shreds.infrastructure.config;

import ai.shreds.infrastructure.exceptions.InfrastructureExceptionScanner;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for security scanning tools.
 * Provides configuration settings for ZAP, Burp Suite, and PETEP scanners.
 */
@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "scanner")
public class InfrastructureScannerConfig {

    private static final String DEFAULT_ZAP_HOST = "localhost";
    private static final int DEFAULT_ZAP_PORT = 8080;
    private static final String DEFAULT_ZAP_API_KEY = "";

    private static final String DEFAULT_BURP_HOST = "localhost";
    private static final int DEFAULT_BURP_PORT = 1337;
    private static final String DEFAULT_BURP_API_KEY = "";

    private static final String DEFAULT_PETEP_HOST = "localhost";
    private static final int DEFAULT_PETEP_PORT = 8000;

    /**
     * ZAP scanner configuration.
     */
    private Map<String, Object> zapConfig = new HashMap<>();

    /**
     * Burp Suite scanner configuration.
     */
    private Map<String, Object> burpConfig = new HashMap<>();

    /**
     * PETEP scanner configuration.
     */
    private Map<String, Object> petepConfig = new HashMap<>();

    /**
     * Initializes default configuration values if not provided.
     */
    @PostConstruct
    public void init() {
        initializeZapConfig();
        initializeBurpConfig();
        initializePetepConfig();
        validateConfigurations();
        logConfigurations();
    }

    private void initializeZapConfig() {
        if (zapConfig == null) {
            zapConfig = new HashMap<>();
        }
        zapConfig.putIfAbsent("host", DEFAULT_ZAP_HOST);
        zapConfig.putIfAbsent("port", DEFAULT_ZAP_PORT);
        zapConfig.putIfAbsent("apiKey", DEFAULT_ZAP_API_KEY);
        zapConfig.putIfAbsent("timeout", 120); // seconds
        zapConfig.putIfAbsent("maxDepth", 10);
        zapConfig.putIfAbsent("threadCount", 5);
    }

    private void initializeBurpConfig() {
        if (burpConfig == null) {
            burpConfig = new HashMap<>();
        }
        burpConfig.putIfAbsent("host", DEFAULT_BURP_HOST);
        burpConfig.putIfAbsent("port", DEFAULT_BURP_PORT);
        burpConfig.putIfAbsent("apiKey", DEFAULT_BURP_API_KEY);
        burpConfig.putIfAbsent("timeout", 180); // seconds
        burpConfig.putIfAbsent("scanSpeed", "normal");
        burpConfig.putIfAbsent("concurrent", true);
    }

    private void initializePetepConfig() {
        if (petepConfig == null) {
            petepConfig = new HashMap<>();
        }
        petepConfig.putIfAbsent("host", DEFAULT_PETEP_HOST);
        petepConfig.putIfAbsent("port", DEFAULT_PETEP_PORT);
        petepConfig.putIfAbsent("timeout", 60); // seconds
        petepConfig.putIfAbsent("interceptMode", "passive");
        petepConfig.putIfAbsent("logLevel", "info");
    }

    /**
     * Validates the configuration settings for all scanners.
     *
     * @throws InfrastructureExceptionScanner if validation fails
     */
    private void validateConfigurations() {
        validateZapConfig();
        validateBurpConfig();
        validatePetepConfig();
    }

    private void validateZapConfig() {
        validateHost(zapConfig.get("host"), "ZAP");
        validatePort(zapConfig.get("port"), "ZAP");
        validateTimeout(zapConfig.get("timeout"), "ZAP");
    }

    private void validateBurpConfig() {
        validateHost(burpConfig.get("host"), "Burp Suite");
        validatePort(burpConfig.get("port"), "Burp Suite");
        validateTimeout(burpConfig.get("timeout"), "Burp Suite");
    }

    private void validatePetepConfig() {
        validateHost(petepConfig.get("host"), "PETEP");
        validatePort(petepConfig.get("port"), "PETEP");
        validateTimeout(petepConfig.get("timeout"), "PETEP");
    }

    private void validateHost(Object host, String scanner) {
        if (!(host instanceof String) || ((String) host).trim().isEmpty()) {
            throw new InfrastructureExceptionScanner(
                    String.format("Invalid host configuration for %s scanner", scanner));
        }
    }

    private void validatePort(Object port, String scanner) {
        if (!(port instanceof Integer) || (Integer) port < 1 || (Integer) port > 65535) {
            throw new InfrastructureExceptionScanner(
                    String.format("Invalid port configuration for %s scanner", scanner));
        }
    }

    private void validateTimeout(Object timeout, String scanner) {
        if (!(timeout instanceof Integer) || (Integer) timeout < 1) {
            throw new InfrastructureExceptionScanner(
                    String.format("Invalid timeout configuration for %s scanner", scanner));
        }
    }

    private void logConfigurations() {
        log.info("ZAP Configuration: host={}, port={}", zapConfig.get("host"), zapConfig.get("port"));
        log.info("Burp Suite Configuration: host={}, port={}", burpConfig.get("host"), burpConfig.get("port"));
        log.info("PETEP Configuration: host={}, port={}", petepConfig.get("host"), petepConfig.get("port"));
    }

    /**
     * Gets the ZAP API URL.
     *
     * @return The complete ZAP API URL
     */
    public String getZapApiUrl() {
        return String.format("http://%s:%d/JSON/", zapConfig.get("host"), zapConfig.get("port"));
    }

    /**
     * Gets the Burp Suite API URL.
     *
     * @return The complete Burp Suite API URL
     */
    public String getBurpApiUrl() {
        return String.format("http://%s:%d/v0.1/", burpConfig.get("host"), burpConfig.get("port"));
    }

    /**
     * Gets the PETEP API URL.
     *
     * @return The complete PETEP API URL
     */
    public String getPetepApiUrl() {
        return String.format("http://%s:%d/api/", petepConfig.get("host"), petepConfig.get("port"));
    }
}

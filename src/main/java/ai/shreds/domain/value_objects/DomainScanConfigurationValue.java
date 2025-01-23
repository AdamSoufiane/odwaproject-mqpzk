package ai.shreds.domain.value_objects;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Value object representing the configuration for a security scan.
 * Contains all parameters needed to configure and execute a security scan.
 */
@Value
@Builder
@Getter(AccessLevel.PUBLIC)
public class DomainScanConfigurationValue {

    /**
     * List of URLs to be scanned.
     */
    List<String> targetUrls;

    /**
     * Credentials for authenticated scanning.
     */
    String credentials;

    /**
     * Depth level for the scan.
     */
    int scanningDepth;

    /**
     * Additional metadata for scan scheduling and configuration.
     */
    Map<String, Object> schedulingMetadata;

    /**
     * Creates a new scan configuration with the specified parameters.
     *
     * @param targetUrls List of URLs to scan
     * @param credentials Authentication credentials
     * @param scanningDepth Depth of scanning
     * @param schedulingMetadata Additional scheduling metadata
     */
    public DomainScanConfigurationValue(List<String> targetUrls, String credentials,
                                       int scanningDepth, Map<String, Object> schedulingMetadata) {
        this.targetUrls = targetUrls != null ? Collections.unmodifiableList(targetUrls) : Collections.emptyList();
        this.credentials = credentials;
        this.scanningDepth = scanningDepth;
        this.schedulingMetadata = schedulingMetadata != null ? 
                Collections.unmodifiableMap(schedulingMetadata) : Collections.emptyMap();
        validate();
    }

    /**
     * Validates the configuration parameters.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (targetUrls == null || targetUrls.isEmpty()) {
            throw new IllegalArgumentException("Target URLs cannot be null or empty");
        }

        if (scanningDepth <= 0) {
            throw new IllegalArgumentException("Scanning depth must be greater than 0");
        }

        if (schedulingMetadata == null) {
            throw new IllegalArgumentException("Scheduling metadata cannot be null");
        }

        // Validate each URL
        targetUrls.forEach(url -> {
            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("Target URL cannot be null or empty");
            }
            if (!url.matches("^[a-zA-Z]+://.*")) {
                throw new IllegalArgumentException("Invalid URL format: " + url);
            }
        });
    }

    /**
     * Checks if authentication credentials are provided.
     *
     * @return true if credentials are present
     */
    public boolean hasCredentials() {
        return credentials != null && !credentials.trim().isEmpty();
    }

    /**
     * Creates a copy of this configuration with a new scanning depth.
     *
     * @param newDepth The new scanning depth
     * @return A new configuration instance
     */
    public DomainScanConfigurationValue withNewDepth(int newDepth) {
        return new DomainScanConfigurationValue(targetUrls, credentials, newDepth, schedulingMetadata);
    }

    /**
     * Creates a copy of this configuration with new credentials.
     *
     * @param newCredentials The new credentials
     * @return A new configuration instance
     */
    public DomainScanConfigurationValue withNewCredentials(String newCredentials) {
        return new DomainScanConfigurationValue(targetUrls, newCredentials, scanningDepth, schedulingMetadata);
    }

    /**
     * Creates a copy of this configuration with additional URLs.
     *
     * @param additionalUrls The URLs to add
     * @return A new configuration instance
     */
    public DomainScanConfigurationValue withAdditionalUrls(List<String> additionalUrls) {
        if (additionalUrls == null || additionalUrls.isEmpty()) {
            return this;
        }
        List<String> newUrls = new java.util.ArrayList<>(targetUrls);
        newUrls.addAll(additionalUrls);
        return new DomainScanConfigurationValue(newUrls, credentials, scanningDepth, schedulingMetadata);
    }

    /**
     * Creates a copy of this configuration with additional metadata.
     *
     * @param additionalMetadata The metadata to add
     * @return A new configuration instance
     */
    public DomainScanConfigurationValue withAdditionalMetadata(Map<String, Object> additionalMetadata) {
        if (additionalMetadata == null || additionalMetadata.isEmpty()) {
            return this;
        }
        Map<String, Object> newMetadata = new java.util.HashMap<>(schedulingMetadata);
        newMetadata.putAll(additionalMetadata);
        return new DomainScanConfigurationValue(targetUrls, credentials, scanningDepth, newMetadata);
    }
}

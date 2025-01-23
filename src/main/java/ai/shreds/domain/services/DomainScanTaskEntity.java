package ai.shreds.domain.services;

import ai.shreds.domain.value_objects.DomainValueScanConfiguration;
import ai.shreds.shared.enums.SharedProtocolTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Domain entity representing a scan task in the scanning service context.
 * Contains the configuration and protocol information needed to execute a security scan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainScanTaskEntity {

    /**
     * Unique identifier for the scan task.
     */
    private String taskId;

    /**
     * Configuration details for the scan.
     */
    private DomainValueScanConfiguration configuration;

    /**
     * List of protocols to be used in scanning.
     */
    private List<SharedProtocolTypeEnum> protocolTypes;

    /**
     * Validates the scan task configuration.
     * Ensures all required fields are present and valid.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        }

        if (configuration == null) {
            throw new IllegalArgumentException("Scan configuration cannot be null");
        }
        configuration.validate(); // Validate the configuration

        if (protocolTypes == null || protocolTypes.isEmpty()) {
            throw new IllegalArgumentException("Protocol types cannot be null or empty");
        }

        // Validate protocol compatibility with target URLs
        configuration.getTargetUrls().forEach(url -> {
            boolean validProtocol = protocolTypes.stream()
                    .anyMatch(protocol -> protocol.matchesUrl(url));
            if (!validProtocol) {
                throw new IllegalArgumentException(
                        String.format("URL %s does not match any of the specified protocols", url));
            }
        });
    }

    /**
     * Checks if this scan task requires authentication.
     *
     * @return true if credentials are provided in the configuration
     */
    public boolean requiresAuthentication() {
        return configuration != null && configuration.hasCredentials();
    }

    /**
     * Checks if this scan task includes secure protocols.
     *
     * @return true if any secure protocol is included
     */
    public boolean includesSecureProtocols() {
        return protocolTypes != null && protocolTypes.stream()
                .anyMatch(SharedProtocolTypeEnum::isSecure);
    }

    /**
     * Gets the maximum scanning depth allowed for this task.
     *
     * @return The configured scanning depth or 1 if not configured
     */
    public int getMaxScanningDepth() {
        return configuration != null ? configuration.getScanningDepth() : 1;
    }

    /**
     * Creates a copy of this entity with a new task ID.
     *
     * @param newTaskId The new task ID to use
     * @return A new DomainScanTaskEntity instance
     */
    public DomainScanTaskEntity withNewTaskId(String newTaskId) {
        return DomainScanTaskEntity.builder()
                .taskId(newTaskId)
                .configuration(this.configuration)
                .protocolTypes(this.protocolTypes)
                .build();
    }
}

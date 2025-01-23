package ai.shreds.domain.entities;

import ai.shreds.domain.services.DomainScanTaskEntity;
import ai.shreds.domain.value_objects.DomainValueScanConfiguration;
import ai.shreds.shared.enums.SharedProtocolTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Domain entity representing a security scan task.
 * Contains all necessary information to perform a security scan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntityScanTask {

    /**
     * Unique identifier for the scan task.
     */
    private String id;

    /**
     * List of URLs to be scanned.
     */
    private List<String> targetUrls;

    /**
     * Credentials for authenticated scanning.
     */
    private String credentials;

    /**
     * Depth level for the scan.
     */
    private int scanningDepth;

    /**
     * List of protocols to be used in scanning.
     */
    private List<SharedProtocolTypeEnum> protocolTypes;

    /**
     * Additional metadata for scan scheduling.
     */
    private Map<String, Object> schedulingMetadata;

    /**
     * Converts this entity to a DomainScanTaskEntity.
     * Creates a new DomainScanTaskEntity with configuration from this entity.
     *
     * @return A new DomainScanTaskEntity instance
     */
    public DomainScanTaskEntity toDomainScanTaskEntity() {
        // Create scan configuration
        DomainValueScanConfiguration configuration = DomainValueScanConfiguration.builder()
                .targetUrls(this.targetUrls)
                .credentials(this.credentials)
                .scanningDepth(this.scanningDepth)
                .schedulingMetadata(this.schedulingMetadata)
                .build();

        // Create and return scan task entity
        return DomainScanTaskEntity.builder()
                .taskId(this.id)
                .configuration(configuration)
                .protocolTypes(this.protocolTypes)
                .build();
    }

    /**
     * Validates the scan task configuration.
     * Checks for required fields and valid values.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (targetUrls == null || targetUrls.isEmpty()) {
            throw new IllegalArgumentException("Target URLs cannot be null or empty");
        }

        if (protocolTypes == null || protocolTypes.isEmpty()) {
            throw new IllegalArgumentException("Protocol types cannot be null or empty");
        }

        if (scanningDepth <= 0) {
            throw new IllegalArgumentException("Scanning depth must be greater than 0");
        }

        if (schedulingMetadata == null) {
            throw new IllegalArgumentException("Scheduling metadata cannot be null");
        }

        // Validate that each URL matches at least one protocol
        targetUrls.forEach(url -> {
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
     * @return true if credentials are provided
     */
    public boolean requiresAuthentication() {
        return credentials != null && !credentials.trim().isEmpty();
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
}

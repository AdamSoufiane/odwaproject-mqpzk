package ai.shreds.domain.entities;

import ai.shreds.domain.services.DomainScanResultEntity;
import ai.shreds.domain.value_objects.DomainValueVulnerability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain entity representing raw scan results.
 * Contains the unprocessed findings and logs from a security scan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntityRawScanResult {

    /**
     * Unique identifier for the scan result.
     */
    private String id;

    /**
     * ID of the associated scan task.
     */
    private String scanTaskId;

    /**
     * List of discovered vulnerabilities.
     */
    @Builder.Default
    private List<DomainValueVulnerability> vulnerabilityFindingsList = new ArrayList<>();

    /**
     * Timestamp when the scan was completed.
     */
    private LocalDateTime timestamp;

    /**
     * Logs generated during scan execution.
     */
    @Builder.Default
    private List<String> scanExecutionLogs = new ArrayList<>();

    /**
     * Validates the scan result.
     * Ensures all required fields are present and valid.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (scanTaskId == null || scanTaskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Scan task ID cannot be null or empty");
        }

        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        if (vulnerabilityFindingsList == null) {
            throw new IllegalArgumentException("Vulnerability findings list cannot be null");
        }

        if (scanExecutionLogs == null || scanExecutionLogs.isEmpty()) {
            throw new IllegalArgumentException("Scan execution logs cannot be empty");
        }

        // Validate each vulnerability
        vulnerabilityFindingsList.forEach(DomainValueVulnerability::validate);
    }

    /**
     * Converts this raw scan result to a domain scan result entity.
     *
     * @return A new DomainScanResultEntity instance
     */
    public DomainScanResultEntity toDomainScanResultEntity() {
        return DomainScanResultEntity.builder()
                .resultId(this.id)
                .scanTaskId(this.scanTaskId)
                .vulnerabilities(new ArrayList<>(this.vulnerabilityFindingsList))
                .timestamp(this.timestamp)
                .executionLogs(new ArrayList<>(this.scanExecutionLogs))
                .build();
    }

    /**
     * Gets high-risk vulnerabilities from the findings.
     *
     * @return List of high-risk vulnerabilities
     */
    public List<DomainValueVulnerability> getHighRiskVulnerabilities() {
        return vulnerabilityFindingsList.stream()
                .filter(DomainValueVulnerability::isHighRisk)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new vulnerability to the findings list.
     *
     * @param vulnerability The vulnerability to add
     */
    public void addVulnerability(DomainValueVulnerability vulnerability) {
        if (vulnerability == null) {
            throw new IllegalArgumentException("Vulnerability cannot be null");
        }
        vulnerability.validate();
        if (vulnerabilityFindingsList == null) {
            vulnerabilityFindingsList = new ArrayList<>();
        }
        vulnerabilityFindingsList.add(vulnerability);
    }

    /**
     * Adds a new execution log entry.
     *
     * @param logEntry The log entry to add
     */
    public void addExecutionLog(String logEntry) {
        if (logEntry == null || logEntry.trim().isEmpty()) {
            throw new IllegalArgumentException("Log entry cannot be null or empty");
        }
        if (scanExecutionLogs == null) {
            scanExecutionLogs = new ArrayList<>();
        }
        scanExecutionLogs.add(String.format("%s - %s", 
                LocalDateTime.now().toString(), logEntry));
    }

    /**
     * Gets an unmodifiable view of the vulnerability findings.
     *
     * @return Unmodifiable list of vulnerabilities
     */
    public List<DomainValueVulnerability> getUnmodifiableVulnerabilityFindings() {
        return Collections.unmodifiableList(vulnerabilityFindingsList);
    }

    /**
     * Gets an unmodifiable view of the execution logs.
     *
     * @return Unmodifiable list of execution logs
     */
    public List<String> getUnmodifiableExecutionLogs() {
        return Collections.unmodifiableList(scanExecutionLogs);
    }
}

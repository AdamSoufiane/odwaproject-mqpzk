package ai.shreds.domain.services;

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
 * Domain entity representing processed scan results.
 * Contains the analyzed and processed findings from a security scan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainScanResultEntity {

    /**
     * Unique identifier for the scan result.
     */
    private String resultId;

    /**
     * ID of the associated scan task.
     */
    private String scanTaskId;

    /**
     * List of discovered vulnerabilities.
     */
    @Builder.Default
    private List<DomainValueVulnerability> vulnerabilities = new ArrayList<>();

    /**
     * Timestamp when the scan was completed.
     */
    private LocalDateTime timestamp;

    /**
     * Logs generated during scan execution.
     */
    @Builder.Default
    private List<String> executionLogs = new ArrayList<>();

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

        if (vulnerabilities == null) {
            throw new IllegalArgumentException("Vulnerabilities list cannot be null");
        }

        if (executionLogs == null || executionLogs.isEmpty()) {
            throw new IllegalArgumentException("Execution logs cannot be empty");
        }

        // Validate each vulnerability
        vulnerabilities.forEach(DomainValueVulnerability::validate);
    }

    /**
     * Gets high-risk vulnerabilities from the findings.
     *
     * @return List of high-risk vulnerabilities
     */
    public List<DomainValueVulnerability> getHighRiskVulnerabilities() {
        return vulnerabilities.stream()
                .filter(DomainValueVulnerability::isHighRisk)
                .collect(Collectors.toList());
    }

    /**
     * Gets vulnerabilities grouped by severity.
     *
     * @return Map of severity to list of vulnerabilities
     */
    public java.util.Map<String, List<DomainValueVulnerability>> getVulnerabilitiesBySeverity() {
        return vulnerabilities.stream()
                .collect(Collectors.groupingBy(v -> v.getSeverity().getValue()));
    }

    /**
     * Adds a new vulnerability to the findings.
     *
     * @param vulnerability The vulnerability to add
     */
    public void addVulnerability(DomainValueVulnerability vulnerability) {
        if (vulnerability == null) {
            throw new IllegalArgumentException("Vulnerability cannot be null");
        }
        vulnerability.validate();
        if (vulnerabilities == null) {
            vulnerabilities = new ArrayList<>();
        }
        vulnerabilities.add(vulnerability);
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
        if (executionLogs == null) {
            executionLogs = new ArrayList<>();
        }
        executionLogs.add(String.format("%s - %s", 
                LocalDateTime.now().toString(), logEntry));
    }

    /**
     * Gets an unmodifiable view of the vulnerabilities.
     *
     * @return Unmodifiable list of vulnerabilities
     */
    public List<DomainValueVulnerability> getUnmodifiableVulnerabilities() {
        return Collections.unmodifiableList(vulnerabilities);
    }

    /**
     * Gets an unmodifiable view of the execution logs.
     *
     * @return Unmodifiable list of execution logs
     */
    public List<String> getUnmodifiableExecutionLogs() {
        return Collections.unmodifiableList(executionLogs);
    }

    /**
     * Gets a summary of the scan results.
     *
     * @return Formatted summary string
     */
    public String getSummary() {
        java.util.Map<String, Long> severityCounts = vulnerabilities.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getSeverity().getValue(),
                        Collectors.counting()));

        return String.format("Scan completed at %s. Found: %s",
                timestamp,
                severityCounts.entrySet().stream()
                        .map(e -> String.format("%d %s", e.getValue(), e.getKey()))
                        .collect(Collectors.joining(", ")));
    }
}

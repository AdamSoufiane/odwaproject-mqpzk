package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.value_objects.DomainValueScanFindings;
import ai.shreds.shared.dtos.SharedScanResultDTO;
import ai.shreds.shared.enums.SharedScanStatusEnum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainEntityRawScanResult {
    private String id;
    private String scanTaskId;
    private List<String> vulnerabilityFindingsList;
    private String timestamp;
    private List<String> scanExecutionLogs;
    private DomainValueScanFindings findings;

    public DomainEntityRawScanResult() {
        this.vulnerabilityFindingsList = new ArrayList<>();
        this.scanExecutionLogs = new ArrayList<>();
        this.findings = new DomainValueScanFindings();
    }

    public DomainEntityRawScanResult(String id,
                                     String scanTaskId,
                                     List<String> vulnerabilityFindingsList,
                                     String timestamp,
                                     List<String> scanExecutionLogs) {
        this.id = id;
        this.scanTaskId = scanTaskId;
        this.vulnerabilityFindingsList = new ArrayList<>(vulnerabilityFindingsList);
        this.timestamp = timestamp;
        this.scanExecutionLogs = new ArrayList<>(scanExecutionLogs);
        this.findings = new DomainValueScanFindings(vulnerabilityFindingsList, this.determineSeverity(vulnerabilityFindingsList));
        validate();
    }

    // Explicit setters to avoid Lombok issues:
    public void setId(String id) {
        this.id = id;
    }
    public void setScanTaskId(String scanTaskId) {
        this.scanTaskId = scanTaskId;
    }
    public void setVulnerabilityFindingsList(List<String> vulnerabilityFindingsList) {
        this.vulnerabilityFindingsList = vulnerabilityFindingsList;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public void setScanExecutionLogs(List<String> scanExecutionLogs) {
        this.scanExecutionLogs = scanExecutionLogs;
    }
    public void setFindings(DomainValueScanFindings findings) {
        this.findings = findings;
    }

    public String getId() {
        return id;
    }
    public String getScanTaskId() {
        return scanTaskId;
    }
    public List<String> getVulnerabilityFindingsList() {
        return Collections.unmodifiableList(vulnerabilityFindingsList);
    }
    public String getTimestamp() {
        return timestamp;
    }
    public List<String> getScanExecutionLogs() {
        return Collections.unmodifiableList(scanExecutionLogs);
    }
    public DomainValueScanFindings getFindings() {
        return findings;
    }

    public void addVulnerabilityFinding(String finding) {
        if (finding == null || finding.trim().isEmpty()) {
            throw new DomainException("Vulnerability finding cannot be null or empty");
        }
        this.vulnerabilityFindingsList.add(finding);
        this.findings = new DomainValueScanFindings(
            this.vulnerabilityFindingsList,
            this.determineSeverity(this.vulnerabilityFindingsList)
        );
    }

    public void addExecutionLog(String log) {
        if (log == null || log.trim().isEmpty()) {
            throw new DomainException("Execution log cannot be null or empty");
        }
        this.scanExecutionLogs.add(log);
    }

    public void validate() {
        if (id == null || id.trim().isEmpty()) {
            throw new DomainException("Result ID is required");
        }
        if (scanTaskId == null || scanTaskId.trim().isEmpty()) {
            throw new DomainException("Scan task ID is required");
        }
        if (vulnerabilityFindingsList == null) {
            throw new DomainException("Vulnerability findings list cannot be null");
        }
        if (scanExecutionLogs == null || scanExecutionLogs.isEmpty()) {
            throw new DomainException("Scan execution logs cannot be empty");
        }
        if (timestamp == null || timestamp.trim().isEmpty()) {
            throw new DomainException("Timestamp is required");
        }
        try {
            LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            throw new DomainException("Invalid timestamp format. Expected ISO-8601 format");
        }
        findings.validate();
    }

    public SharedScanResultDTO toSharedDTO() {
        return new SharedScanResultDTO(
            SharedScanStatusEnum.COMPLETED,
            this.id,
            String.format("Scan completed with %d findings", this.vulnerabilityFindingsList.size()),
            this.scanTaskId
        );
    }

    public void updateFindings() {
        this.findings = new DomainValueScanFindings(
            this.vulnerabilityFindingsList,
            this.determineSeverity(this.vulnerabilityFindingsList)
        );
        this.findings.validate();
    }

    private ai.shreds.domain.value_objects.DomainValueSeverityEnum determineSeverity(List<String> findingsList) {
        if (findingsList != null && !findingsList.isEmpty()) {
            return ai.shreds.domain.value_objects.DomainValueSeverityEnum.HIGH;
        } else {
            return ai.shreds.domain.value_objects.DomainValueSeverityEnum.LOW;
        }
    }
}

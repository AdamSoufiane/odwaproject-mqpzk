package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedScanStatusEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO representing the result of a scan operation.
 */
public class SharedScanResultDTO {

    @NotNull(message = "Status cannot be null")
    private SharedScanStatusEnum status;

    @NotBlank(message = "Result ID cannot be blank")
    private String resultId;

    @NotBlank(message = "Summary cannot be blank")
    private String summary;

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    // Explicit no-args constructor
    public SharedScanResultDTO() {
    }

    // Explicit all-args constructor
    public SharedScanResultDTO(SharedScanStatusEnum status,
                               String resultId,
                               String summary,
                               String scanTaskId) {
        this.status = status;
        this.resultId = resultId;
        this.summary = summary;
        this.scanTaskId = scanTaskId;
    }

    public SharedScanStatusEnum getStatus() {
        return status;
    }

    public void setStatus(SharedScanStatusEnum status) {
        this.status = status;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getScanTaskId() {
        return scanTaskId;
    }

    public void setScanTaskId(String scanTaskId) {
        this.scanTaskId = scanTaskId;
    }
}

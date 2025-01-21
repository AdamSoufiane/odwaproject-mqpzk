package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedScanStatusEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO representing the response or status of a scan task.
 */
public class SharedScanTaskDTO {

    @NotNull(message = "Status cannot be null")
    private SharedScanStatusEnum status;

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    @NotBlank(message = "Message cannot be blank")
    private String message;

    // Explicit no-args constructor
    public SharedScanTaskDTO() {
    }

    // Explicit all-args constructor
    public SharedScanTaskDTO(SharedScanStatusEnum status, String scanTaskId, String message) {
        this.status = status;
        this.scanTaskId = scanTaskId;
        this.message = message;
    }

    public SharedScanStatusEnum getStatus() {
        return status;
    }

    public void setStatus(SharedScanStatusEnum status) {
        this.status = status;
    }

    public String getScanTaskId() {
        return scanTaskId;
    }

    public void setScanTaskId(String scanTaskId) {
        this.scanTaskId = scanTaskId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

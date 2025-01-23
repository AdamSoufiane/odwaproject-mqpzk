package ai.shreds.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of possible scan task statuses.
 * Used to indicate the current state of a scan task.
 */
public enum SharedScanTaskStatusEnum {
    RECEIVED("RECEIVED", "Scan task has been received"),
    VALIDATED("VALIDATED", "Scan task has been validated"),
    IN_PROGRESS("IN_PROGRESS", "Scan is currently in progress"),
    COMPLETED("COMPLETED", "Scan has been completed successfully"),
    FAILED("FAILED", "Scan has failed"),
    INVALID("INVALID", "Scan task is invalid");

    private final String value;
    private final String description;

    SharedScanTaskStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Checks if this status represents a terminal state.
     *
     * @return true if the status is terminal (COMPLETED, FAILED, INVALID)
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == INVALID;
    }

    /**
     * Checks if this status represents a successful completion.
     *
     * @return true if the status is COMPLETED
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
}

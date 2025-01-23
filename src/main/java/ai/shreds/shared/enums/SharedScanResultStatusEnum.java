package ai.shreds.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of possible scan result statuses.
 * Used to indicate the status of a scan result processing operation.
 */
public enum SharedScanResultStatusEnum {
    PROCESSED("PROCESSED", "Scan result has been successfully processed"),
    STORED("STORED", "Scan result has been stored in the database"),
    INVALID("INVALID", "Scan result is invalid or malformed"),
    ERROR("ERROR", "Error occurred while processing scan result");

    private final String value;
    private final String description;

    SharedScanResultStatusEnum(String value, String description) {
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
     * Checks if this status represents a successful operation.
     *
     * @return true if the status is PROCESSED or STORED
     */
    public boolean isSuccessful() {
        return this == PROCESSED || this == STORED;
    }

    /**
     * Checks if this status represents an error condition.
     *
     * @return true if the status is ERROR or INVALID
     */
    public boolean isError() {
        return this == ERROR || this == INVALID;
    }
}

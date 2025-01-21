package ai.shreds.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the possible states of a scan task or result.
 */
public enum SharedScanStatusEnum {
    
    PENDING("PENDING", "Scan is waiting to be processed"),
    IN_PROGRESS("IN_PROGRESS", "Scan is currently being executed"),
    COMPLETED("COMPLETED", "Scan has completed successfully"),
    FAILED("FAILED", "Scan has failed"),
    CANCELLED("CANCELLED", "Scan was cancelled");

    private final String value;
    private final String description;

    SharedScanStatusEnum(String value, String description) {
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

    public static SharedScanStatusEnum fromValue(String value) {
        for (SharedScanStatusEnum status : SharedScanStatusEnum.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}

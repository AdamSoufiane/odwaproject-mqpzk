package ai.shreds.application.exceptions;

/**
 * Enum containing all possible error codes for the application layer.
 */
public enum ApplicationErrorCode {
    
    // Scan Task related errors
    NULL_SCAN_TASK_REQUEST("SCAN-001", "Scan task request cannot be null"),
    MISSING_SCAN_TASK_ID("SCAN-002", "Scan task ID is required"),
    MISSING_TARGET_URLS("SCAN-003", "Target URLs are required"),
    MISSING_PROTOCOL_TYPES("SCAN-004", "Protocol types are required"),
    INVALID_SCANNING_DEPTH("SCAN-005", "Invalid scanning depth"),
    MISSING_SCHEDULING_METADATA("SCAN-006", "Scheduling metadata is required"),
    UNAUTHORIZED_SCAN_TASK("SCAN-007", "Unauthorized scan task"),
    SCAN_TASK_PROCESSING_ERROR("SCAN-008", "Failed to process scan task"),

    // Scan Result related errors
    NULL_SCAN_COMPLETION_REQUEST("RESULT-001", "Scan completion request cannot be null"),
    NULL_VULNERABILITY_FINDINGS("RESULT-002", "Vulnerability findings list cannot be null"),
    MISSING_SCAN_LOGS("RESULT-003", "Scan execution logs are required"),
    MISSING_TIMESTAMP("RESULT-004", "Timestamp is required"),
    SCAN_RESULT_PROCESSING_ERROR("RESULT-005", "Failed to process scan result");

    private final String code;
    private final String defaultMessage;

    ApplicationErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}

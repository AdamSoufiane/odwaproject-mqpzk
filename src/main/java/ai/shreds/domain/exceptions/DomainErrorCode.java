package ai.shreds.domain.exceptions;

/**
 * Enum containing all possible error codes for the domain layer.
 */
public enum DomainErrorCode {
    
    // Scan Task related errors
    NULL_SCAN_TASK("DOM-001", "Scan task cannot be null"),
    MISSING_TASK_ID("DOM-002", "Scan task ID is required"),
    MISSING_TARGET_URLS("DOM-003", "Target URLs are required"),
    MISSING_PROTOCOLS("DOM-004", "Protocol types are required"),
    INVALID_PROTOCOL("DOM-005", "Invalid protocol specified"),
    INVALID_SCAN_DEPTH("DOM-006", "Invalid scanning depth"),
    UNAUTHORIZED_SCAN("DOM-007", "Unauthorized scan attempt"),
    SCAN_EXECUTION_ERROR("DOM-008", "Failed to execute scan"),

    // Protocol Scan related errors
    PROTOCOL_EXECUTION_ERROR("DOM-101", "Failed to execute protocols"),
    SCANNER_ERROR("DOM-102", "Error during protocol scanning"),

    // Repository related errors
    TASK_SAVE_ERROR("DOM-201", "Failed to save scan task"),
    RESULT_SAVE_ERROR("DOM-202", "Failed to save scan result"),
    TASK_RETRIEVAL_ERROR("DOM-203", "Failed to retrieve scan task"),
    RESULT_RETRIEVAL_ERROR("DOM-204", "Failed to retrieve scan results"),

    // Authentication related errors
    AUTH_SERVICE_ERROR("DOM-301", "Error communicating with auth service"),
    INVALID_CREDENTIALS("DOM-302", "Invalid credentials provided");

    private final String code;
    private final String defaultMessage;

    DomainErrorCode(String code, String defaultMessage) {
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

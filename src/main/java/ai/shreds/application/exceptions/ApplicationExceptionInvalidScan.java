package ai.shreds.application.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a scan configuration or execution is invalid.
 * This exception indicates issues with scan configuration, execution,
 * or processing that are beyond simple validation failures.
 */
@Getter
public class ApplicationExceptionInvalidScan extends RuntimeException {

    private final String scanId;
    private final String scanType;
    private final String errorCode;

    /**
     * Creates a new invalid scan exception with the specified message.
     *
     * @param message Detailed error message
     */
    public ApplicationExceptionInvalidScan(String message) {
        super(message);
        this.scanId = null;
        this.scanType = null;
        this.errorCode = "UNKNOWN";
    }

    /**
     * Creates a new invalid scan exception with the specified message and cause.
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the error
     */
    public ApplicationExceptionInvalidScan(String message, Throwable cause) {
        super(message, cause);
        this.scanId = null;
        this.scanType = null;
        this.errorCode = "UNKNOWN";
    }

    /**
     * Creates a new invalid scan exception with scan-specific information.
     *
     * @param message Detailed error message
     * @param scanId ID of the scan that caused the error
     * @param scanType Type of scan being performed
     * @param errorCode Specific error code for the failure
     */
    public ApplicationExceptionInvalidScan(String message, String scanId, String scanType, String errorCode) {
        super(String.format("%s (Scan ID: %s, Type: %s, Error Code: %s)", 
                message, scanId, scanType, errorCode));
        this.scanId = scanId;
        this.scanType = scanType;
        this.errorCode = errorCode;
    }

    /**
     * Creates a new invalid scan exception with scan-specific information and cause.
     *
     * @param message Detailed error message
     * @param scanId ID of the scan that caused the error
     * @param scanType Type of scan being performed
     * @param errorCode Specific error code for the failure
     * @param cause The underlying cause of the error
     */
    public ApplicationExceptionInvalidScan(String message, String scanId, String scanType, 
                                          String errorCode, Throwable cause) {
        super(String.format("%s (Scan ID: %s, Type: %s, Error Code: %s)", 
                message, scanId, scanType, errorCode), cause);
        this.scanId = scanId;
        this.scanType = scanType;
        this.errorCode = errorCode;
    }

    /**
     * Checks if this exception has scan-specific information.
     *
     * @return true if scanId and scanType are available
     */
    public boolean hasScanInfo() {
        return scanId != null && scanType != null;
    }

    /**
     * Gets a formatted string containing all available error details.
     *
     * @return Formatted error details string
     */
    public String getErrorDetails() {
        if (!hasScanInfo()) {
            return getMessage();
        }
        return String.format("Scan Error - ID: %s, Type: %s, Code: %s, Message: %s",
                scanId, scanType, errorCode, getMessage());
    }
}

package ai.shreds.infrastructure.exceptions;

import lombok.Getter;

/**
 * Exception thrown when scanner operations fail.
 * This exception indicates failures in scanner initialization, configuration,
 * or execution of security scans.
 */
@Getter
public class InfrastructureExceptionScanner extends RuntimeException {

    private final String scannerName;
    private final String operation;
    private final String errorCode;

    /**
     * Creates a new scanner exception with the specified message.
     *
     * @param message Detailed error message
     */
    public InfrastructureExceptionScanner(String message) {
        super(message);
        this.scannerName = null;
        this.operation = null;
        this.errorCode = "UNKNOWN";
    }

    /**
     * Creates a new scanner exception with the specified message and cause.
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the error
     */
    public InfrastructureExceptionScanner(String message, Throwable cause) {
        super(message, cause);
        this.scannerName = null;
        this.operation = null;
        this.errorCode = "UNKNOWN";
    }

    /**
     * Creates a new scanner exception with scanner-specific information.
     *
     * @param message Detailed error message
     * @param scannerName Name of the scanner that failed
     * @param operation Operation being performed
     * @param errorCode Specific error code
     */
    public InfrastructureExceptionScanner(String message, String scannerName, 
                                         String operation, String errorCode) {
        super(String.format("%s (Scanner: %s, Operation: %s, Error Code: %s)", 
                message, scannerName, operation, errorCode));
        this.scannerName = scannerName;
        this.operation = operation;
        this.errorCode = errorCode;
    }

    /**
     * Creates a new scanner exception with scanner-specific information and cause.
     *
     * @param message Detailed error message
     * @param scannerName Name of the scanner that failed
     * @param operation Operation being performed
     * @param errorCode Specific error code
     * @param cause The underlying cause of the error
     */
    public InfrastructureExceptionScanner(String message, String scannerName, 
                                         String operation, String errorCode, 
                                         Throwable cause) {
        super(String.format("%s (Scanner: %s, Operation: %s, Error Code: %s)", 
                message, scannerName, operation, errorCode), cause);
        this.scannerName = scannerName;
        this.operation = operation;
        this.errorCode = errorCode;
    }

    /**
     * Checks if this exception has scanner-specific information.
     *
     * @return true if scannerName and operation are available
     */
    public boolean hasScannerInfo() {
        return scannerName != null && operation != null;
    }

    /**
     * Gets a formatted string containing all error details.
     *
     * @return Formatted error details string
     */
    public String getErrorDetails() {
        if (!hasScannerInfo()) {
            return getMessage();
        }
        return String.format("Scanner Error - Name: %s, Operation: %s, Code: %s, Message: %s",
                scannerName, operation, errorCode, getMessage());
    }

    /**
     * Creates a new exception instance for initialization errors.
     *
     * @param scannerName Name of the scanner
     * @param cause The underlying cause
     * @return A new exception instance
     */
    public static InfrastructureExceptionScanner initializationError(String scannerName, 
                                                                    Throwable cause) {
        return new InfrastructureExceptionScanner(
                String.format("Failed to initialize scanner: %s", scannerName),
                scannerName, "INITIALIZATION", "INIT_ERROR", cause);
    }

    /**
     * Creates a new exception instance for configuration errors.
     *
     * @param scannerName Name of the scanner
     * @param configItem The configuration item that failed
     * @return A new exception instance
     */
    public static InfrastructureExceptionScanner configurationError(String scannerName, 
                                                                   String configItem) {
        return new InfrastructureExceptionScanner(
                String.format("Invalid configuration for %s: %s", scannerName, configItem),
                scannerName, "CONFIGURATION", "CONFIG_ERROR");
    }

    /**
     * Creates a new exception instance for connection errors.
     *
     * @param scannerName Name of the scanner
     * @param cause The underlying cause
     * @return A new exception instance
     */
    public static InfrastructureExceptionScanner connectionError(String scannerName, 
                                                                Throwable cause) {
        return new InfrastructureExceptionScanner(
                String.format("Failed to connect to scanner: %s", scannerName),
                scannerName, "CONNECTION", "CONN_ERROR", cause);
    }
}

package ai.shreds.adapter.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a scan message (task or result) is invalid.
 * This exception is used in the adapter layer to indicate validation failures
 * before the message reaches the application layer.
 */
@Getter
public class AdapterExceptionInvalidScanMessage extends RuntimeException {

    private final String scanId;

    /**
     * Creates a new exception with the specified error message.
     *
     * @param message Detailed error message
     */
    public AdapterExceptionInvalidScanMessage(String message) {
        super(message);
        this.scanId = null;
    }

    /**
     * Creates a new exception with the specified error message and scan ID.
     *
     * @param message Detailed error message
     * @param scanId ID of the scan task or result that caused the error
     */
    public AdapterExceptionInvalidScanMessage(String message, String scanId) {
        super(String.format("Invalid scan message for scan ID %s: %s", scanId, message));
        this.scanId = scanId;
    }

    /**
     * Creates a new exception with the specified error message, cause, and scan ID.
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the error
     * @param scanId ID of the scan task or result that caused the error
     */
    public AdapterExceptionInvalidScanMessage(String message, Throwable cause, String scanId) {
        super(String.format("Invalid scan message for scan ID %s: %s", scanId, message), cause);
        this.scanId = scanId;
    }

    /**
     * Creates a new exception with the specified error message and cause.
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the error
     */
    public AdapterExceptionInvalidScanMessage(String message, Throwable cause) {
        super(message, cause);
        this.scanId = null;
    }
}

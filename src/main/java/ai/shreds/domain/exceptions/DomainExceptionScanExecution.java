package ai.shreds.domain.exceptions;

import lombok.Getter;

/**
 * Exception thrown when scan execution fails in the domain layer.
 * This exception indicates failures during the actual execution of security scans,
 * including integration errors with scanning tools.
 */
@Getter
public class DomainExceptionScanExecution extends RuntimeException {

    private final String scanId;
    private final String toolName;
    private final String errorCode;
    private final String phase;

    /**
     * Creates a new scan execution exception with the specified message.
     *
     * @param message Detailed error message
     */
    public DomainExceptionScanExecution(String message) {
        super(message);
        this.scanId = null;
        this.toolName = null;
        this.errorCode = "UNKNOWN";
        this.phase = null;
    }

    /**
     * Creates a new scan execution exception with the specified message and cause.
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the execution failure
     */
    public DomainExceptionScanExecution(String message, Throwable cause) {
        super(message, cause);
        this.scanId = null;
        this.toolName = null;
        this.errorCode = "UNKNOWN";
        this.phase = null;
    }

    /**
     * Creates a new scan execution exception with execution details.
     *
     * @param message Detailed error message
     * @param scanId ID of the scan that failed
     * @param toolName Name of the scanning tool that failed
     * @param errorCode Specific error code for the failure
     * @param phase Phase of execution where the failure occurred
     */
    public DomainExceptionScanExecution(String message, String scanId, 
                                       String toolName, String errorCode, String phase) {
        super(String.format("%s (Scan ID: %s, Tool: %s, Error Code: %s, Phase: %s)", 
                message, scanId, toolName, errorCode, phase));
        this.scanId = scanId;
        this.toolName = toolName;
        this.errorCode = errorCode;
        this.phase = phase;
    }

    /**
     * Creates a new scan execution exception with execution details and cause.
     *
     * @param message Detailed error message
     * @param scanId ID of the scan that failed
     * @param toolName Name of the scanning tool that failed
     * @param errorCode Specific error code for the failure
     * @param phase Phase of execution where the failure occurred
     * @param cause The underlying cause of the execution failure
     */
    public DomainExceptionScanExecution(String message, String scanId, 
                                       String toolName, String errorCode, 
                                       String phase, Throwable cause) {
        super(String.format("%s (Scan ID: %s, Tool: %s, Error Code: %s, Phase: %s)", 
                message, scanId, toolName, errorCode, phase), cause);
        this.scanId = scanId;
        this.toolName = toolName;
        this.errorCode = errorCode;
        this.phase = phase;
    }

    /**
     * Checks if this exception contains execution details.
     *
     * @return true if scanId and toolName are available
     */
    public boolean hasExecutionDetails() {
        return scanId != null && toolName != null;
    }

    /**
     * Gets a formatted string containing all execution details.
     *
     * @return Formatted execution details string
     */
    public String getExecutionDetails() {
        if (!hasExecutionDetails()) {
            return getMessage();
        }
        return String.format("Execution Error - Scan ID: %s, Tool: %s, Error Code: %s, Phase: %s, Message: %s",
                scanId, toolName, errorCode, phase, getMessage());
    }

    /**
     * Creates a new exception instance for tool initialization failures.
     *
     * @param toolName Name of the tool that failed to initialize
     * @param cause The underlying cause
     * @return A new exception instance
     */
    public static DomainExceptionScanExecution toolInitializationError(String toolName, Throwable cause) {
        return new DomainExceptionScanExecution(
                String.format("Failed to initialize scanning tool: %s", toolName),
                null, toolName, "INIT_ERROR", "INITIALIZATION", cause);
    }

    /**
     * Creates a new exception instance for scan timeout errors.
     *
     * @param scanId ID of the scan that timed out
     * @param toolName Name of the tool that timed out
     * @return A new exception instance
     */
    public static DomainExceptionScanExecution scanTimeout(String scanId, String toolName) {
        return new DomainExceptionScanExecution(
                "Scan execution timed out",
                scanId, toolName, "TIMEOUT", "EXECUTION");
    }
}

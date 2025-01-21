package ai.shreds.application.exceptions;

import lombok.Getter;

/**
 * Exception thrown by the application layer when scan-related operations fail.
 */
@Getter
public class ApplicationScanException extends RuntimeException {

    private final String errorCode;
    private final ApplicationErrorCode applicationErrorCode;

    public ApplicationScanException(String message, ApplicationErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.applicationErrorCode = errorCode;
    }

    public ApplicationScanException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.applicationErrorCode = null;
    }

    public ApplicationScanException(String message, ApplicationErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
        this.applicationErrorCode = errorCode;
    }

    public ApplicationScanException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.applicationErrorCode = null;
    }
}

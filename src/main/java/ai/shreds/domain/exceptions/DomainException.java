package ai.shreds.domain.exceptions;

import lombok.Getter;

/**
 * Exception thrown by the domain layer when business rules are violated
 * or domain operations fail.
 */
@Getter
public class DomainException extends RuntimeException {

    private final String errorCode;
    private final DomainErrorCode domainErrorCode;

    public DomainException(String message, DomainErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.domainErrorCode = errorCode;
    }

    public DomainException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.domainErrorCode = null;
    }

    public DomainException(String message, DomainErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
        this.domainErrorCode = errorCode;
    }

    public DomainException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.domainErrorCode = null;
    }

    public DomainException(String message) {
        super(message);
        this.errorCode = "UNKNOWN";
        this.domainErrorCode = null;
    }
}

package ai.shreds.infrastructure.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InfrastructureException extends RuntimeException {

    private final String errorCode;
    private final InfrastructureErrorCode infrastructureErrorCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    public InfrastructureException(String message, InfrastructureErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.infrastructureErrorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public InfrastructureException(String message, InfrastructureErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
        this.infrastructureErrorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public InfrastructureException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.infrastructureErrorCode = null;
        this.timestamp = LocalDateTime.now();
    }

    public InfrastructureException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.infrastructureErrorCode = null;
        this.timestamp = LocalDateTime.now();
    }

    // Explicit getter to guarantee existence
    public String getErrorCode() {
        return errorCode;
    }
}

package ai.shreds.infrastructure.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Standard error response format for REST APIs.
 */
@Getter
public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final String details;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    public ErrorResponse(String errorCode, String message) {
        this(errorCode, message, null);
    }

    public ErrorResponse(String errorCode, String message, String details) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse fromException(InfrastructureException ex) {
        // Ensure ex.getErrorCode() is recognized
        return new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getCause() != null ? ex.getCause().getMessage() : null
        );
    }
}

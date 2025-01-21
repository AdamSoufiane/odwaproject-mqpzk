package ai.shreds.adapter.exceptions;

import lombok.Getter;

@Getter
public class AdapterException extends RuntimeException {
    private final String errorCode;

    public AdapterException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AdapterException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

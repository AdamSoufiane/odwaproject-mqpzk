package ai.shreds.application.exceptions;

import lombok.Getter;

/**
 * Exception thrown when validation fails in the application layer.
 * This exception is used to indicate validation failures of business rules
 * and data constraints beyond simple field validation.
 */
@Getter
public class ApplicationExceptionValidation extends RuntimeException {

    private final String field;
    private final Object invalidValue;

    /**
     * Creates a new validation exception with the specified message.
     *
     * @param message Detailed error message
     */
    public ApplicationExceptionValidation(String message) {
        super(message);
        this.field = null;
        this.invalidValue = null;
    }

    /**
     * Creates a new validation exception with the specified message and cause.
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the validation failure
     */
    public ApplicationExceptionValidation(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.invalidValue = null;
    }

    /**
     * Creates a new validation exception with field-specific information.
     *
     * @param message Detailed error message
     * @param field The field that failed validation
     * @param invalidValue The invalid value that caused the validation failure
     */
    public ApplicationExceptionValidation(String message, String field, Object invalidValue) {
        super(String.format("%s (Field: %s, Invalid value: %s)", message, field, invalidValue));
        this.field = field;
        this.invalidValue = invalidValue;
    }

    /**
     * Creates a new validation exception with field-specific information and cause.
     *
     * @param message Detailed error message
     * @param field The field that failed validation
     * @param invalidValue The invalid value that caused the validation failure
     * @param cause The underlying cause of the validation failure
     */
    public ApplicationExceptionValidation(String message, String field, Object invalidValue, Throwable cause) {
        super(String.format("%s (Field: %s, Invalid value: %s)", message, field, invalidValue), cause);
        this.field = field;
        this.invalidValue = invalidValue;
    }
}

package ai.shreds.domain.exceptions;

import lombok.Getter;

/**
 * Exception thrown when scan validation fails in the domain layer.
 * This exception indicates that a scan configuration or parameters
 * failed domain-specific validation rules.
 */
@Getter
public class DomainExceptionScanValidation extends RuntimeException {

    private final String scanId;
    private final String validationRule;
    private final String invalidValue;

    /**
     * Creates a new scan validation exception with the specified message.
     *
     * @param message Detailed error message
     */
    public DomainExceptionScanValidation(String message) {
        super(message);
        this.scanId = null;
        this.validationRule = null;
        this.invalidValue = null;
    }

    /**
     * Creates a new scan validation exception with the specified message and cause.
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the validation failure
     */
    public DomainExceptionScanValidation(String message, Throwable cause) {
        super(message, cause);
        this.scanId = null;
        this.validationRule = null;
        this.invalidValue = null;
    }

    /**
     * Creates a new scan validation exception with validation details.
     *
     * @param message Detailed error message
     * @param scanId ID of the scan that failed validation
     * @param validationRule The validation rule that failed
     * @param invalidValue The invalid value that caused the failure
     */
    public DomainExceptionScanValidation(String message, String scanId, 
                                        String validationRule, String invalidValue) {
        super(String.format("%s (Scan ID: %s, Rule: %s, Invalid Value: %s)", 
                message, scanId, validationRule, invalidValue));
        this.scanId = scanId;
        this.validationRule = validationRule;
        this.invalidValue = invalidValue;
    }

    /**
     * Creates a new scan validation exception with validation details and cause.
     *
     * @param message Detailed error message
     * @param scanId ID of the scan that failed validation
     * @param validationRule The validation rule that failed
     * @param invalidValue The invalid value that caused the failure
     * @param cause The underlying cause of the validation failure
     */
    public DomainExceptionScanValidation(String message, String scanId, 
                                        String validationRule, String invalidValue, 
                                        Throwable cause) {
        super(String.format("%s (Scan ID: %s, Rule: %s, Invalid Value: %s)", 
                message, scanId, validationRule, invalidValue), cause);
        this.scanId = scanId;
        this.validationRule = validationRule;
        this.invalidValue = invalidValue;
    }

    /**
     * Checks if this exception contains validation details.
     *
     * @return true if scanId and validationRule are available
     */
    public boolean hasValidationDetails() {
        return scanId != null && validationRule != null;
    }

    /**
     * Gets a formatted string containing all validation details.
     *
     * @return Formatted validation details string
     */
    public String getValidationDetails() {
        if (!hasValidationDetails()) {
            return getMessage();
        }
        return String.format("Validation Error - Scan ID: %s, Rule: %s, Invalid Value: %s, Message: %s",
                scanId, validationRule, invalidValue, getMessage());
    }
}

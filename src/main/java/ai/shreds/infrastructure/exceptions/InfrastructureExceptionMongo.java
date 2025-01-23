package ai.shreds.infrastructure.exceptions;

import lombok.Getter;

/**
 * Exception thrown when MongoDB operations fail.
 * This exception wraps MongoDB-specific errors and provides additional context.
 */
@Getter
public class InfrastructureExceptionMongo extends RuntimeException {

    private final String collection;
    private final String operation;
    private final String errorCode;

    /**
     * Creates a new MongoDB exception with the specified message.
     *
     * @param message Detailed error message
     */
    public InfrastructureExceptionMongo(String message) {
        super(message);
        this.collection = null;
        this.operation = null;
        this.errorCode = "UNKNOWN";
    }

    /**
     * Creates a new MongoDB exception with the specified message and cause.
     *
     * @param message Detailed error message
     * @param cause The underlying cause of the error
     */
    public InfrastructureExceptionMongo(String message, Throwable cause) {
        super(message, cause);
        this.collection = null;
        this.operation = null;
        this.errorCode = "UNKNOWN";
    }

    /**
     * Creates a new MongoDB exception with operation details.
     *
     * @param message Detailed error message
     * @param collection The MongoDB collection being accessed
     * @param operation The operation being performed
     * @param errorCode Specific error code
     */
    public InfrastructureExceptionMongo(String message, String collection, 
                                       String operation, String errorCode) {
        super(String.format("%s (Collection: %s, Operation: %s, Error Code: %s)", 
                message, collection, operation, errorCode));
        this.collection = collection;
        this.operation = operation;
        this.errorCode = errorCode;
    }

    /**
     * Creates a new MongoDB exception with operation details and cause.
     *
     * @param message Detailed error message
     * @param collection The MongoDB collection being accessed
     * @param operation The operation being performed
     * @param errorCode Specific error code
     * @param cause The underlying cause of the error
     */
    public InfrastructureExceptionMongo(String message, String collection, 
                                       String operation, String errorCode, 
                                       Throwable cause) {
        super(String.format("%s (Collection: %s, Operation: %s, Error Code: %s)", 
                message, collection, operation, errorCode), cause);
        this.collection = collection;
        this.operation = operation;
        this.errorCode = errorCode;
    }

    /**
     * Checks if this exception has operation details.
     *
     * @return true if collection and operation are available
     */
    public boolean hasOperationDetails() {
        return collection != null && operation != null;
    }

    /**
     * Gets a formatted string containing all error details.
     *
     * @return Formatted error details string
     */
    public String getErrorDetails() {
        if (!hasOperationDetails()) {
            return getMessage();
        }
        return String.format("MongoDB Error - Collection: %s, Operation: %s, Code: %s, Message: %s",
                collection, operation, errorCode, getMessage());
    }

    /**
     * Creates a new exception instance for connection errors.
     *
     * @param host The MongoDB host
     * @param cause The underlying cause
     * @return A new exception instance
     */
    public static InfrastructureExceptionMongo connectionError(String host, Throwable cause) {
        return new InfrastructureExceptionMongo(
                String.format("Failed to connect to MongoDB host: %s", host),
                "N/A", "CONNECT", "CONNECTION_ERROR", cause);
    }

    /**
     * Creates a new exception instance for authentication errors.
     *
     * @param database The database name
     * @param cause The underlying cause
     * @return A new exception instance
     */
    public static InfrastructureExceptionMongo authenticationError(String database, Throwable cause) {
        return new InfrastructureExceptionMongo(
                String.format("Authentication failed for database: %s", database),
                "N/A", "AUTHENTICATE", "AUTH_ERROR", cause);
    }
}

package ai.shreds.infrastructure.exceptions;

/**
 * Enum containing all possible error codes for the infrastructure layer.
 */
public enum InfrastructureErrorCode {
    
    // MongoDB related errors
    MONGO_CONNECTION_ERROR("INFRA-001", "Failed to connect to MongoDB"),
    MONGO_QUERY_ERROR("INFRA-002", "Error executing MongoDB query"),
    MONGO_WRITE_ERROR("INFRA-003", "Error writing to MongoDB"),

    // RabbitMQ related errors
    RABBITMQ_CONNECTION_ERROR("INFRA-101", "Failed to connect to RabbitMQ"),
    RABBITMQ_PUBLISH_ERROR("INFRA-102", "Error publishing message to RabbitMQ"),
    RABBITMQ_CONSUME_ERROR("INFRA-103", "Error consuming message from RabbitMQ"),

    // External Service related errors
    AUTH_SERVICE_CONNECTION_ERROR("INFRA-201", "Failed to connect to Auth Service"),
    SCANNER_SERVICE_ERROR("INFRA-202", "Error in Scanner Service"),

    // General infrastructure errors
    SERIALIZATION_ERROR("INFRA-301", "Error serializing/deserializing data"),
    CONFIGURATION_ERROR("INFRA-302", "Invalid configuration");

    private final String code;
    private final String defaultMessage;

    InfrastructureErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}

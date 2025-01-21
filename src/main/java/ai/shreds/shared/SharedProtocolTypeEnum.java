package ai.shreds.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the supported protocols for the security scan.
 * Each protocol type represents a different method of communication
 * that will be tested during the security scanning process.
 */
public enum SharedProtocolTypeEnum {
    
    /**
     * Standard HTTP protocol (port 80)
     * Used for basic web communication without encryption
     */
    HTTP("http"),

    /**
     * Secure HTTP protocol (port 443)
     * Used for encrypted web communication
     */
    HTTPS("https"),

    /**
     * File Transfer Protocol (port 21)
     * Used for file transfer operations
     */
    FTP("ftp");

    private final String value;

    SharedProtocolTypeEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Converts a string value to its corresponding enum value.
     *
     * @param value the string value to convert
     * @return the corresponding SharedProtocolTypeEnum
     * @throws IllegalArgumentException if no matching enum is found
     */
    public static SharedProtocolTypeEnum fromValue(String value) {
        for (SharedProtocolTypeEnum type : SharedProtocolTypeEnum.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown protocol type: " + value);
    }
}

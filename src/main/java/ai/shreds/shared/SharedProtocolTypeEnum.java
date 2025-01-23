package ai.shreds.shared.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of supported protocols for security scanning.
 * Used to specify which protocols should be included in the security scan.
 */
public enum SharedProtocolTypeEnum {
    HTTP("http"),
    HTTPS("https"),
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
     * Checks if the protocol is secure (HTTPS).
     *
     * @return true if the protocol is HTTPS, false otherwise
     */
    public boolean isSecure() {
        return this == HTTPS;
    }

    /**
     * Gets default port for the protocol.
     *
     * @return default port number for the protocol
     */
    public int getDefaultPort() {
        return switch (this) {
            case HTTP -> 80;
            case HTTPS -> 443;
            case FTP -> 21;
        };
    }

    /**
     * Validates if the given URL matches this protocol.
     *
     * @param url URL to validate
     * @return true if URL matches this protocol, false otherwise
     */
    public boolean matchesUrl(String url) {
        return url != null && url.toLowerCase().startsWith(this.value + "://");
    }
}

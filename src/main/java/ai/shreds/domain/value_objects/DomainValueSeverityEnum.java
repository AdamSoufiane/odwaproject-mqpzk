package ai.shreds.domain.value_objects;

/**
 * Enum representing the severity levels of security findings.
 */
public enum DomainValueSeverityEnum {
    CRITICAL("CRITICAL", 1),
    HIGH("HIGH", 2),
    MEDIUM("MEDIUM", 3),
    LOW("LOW", 4),
    INFO("INFO", 5);

    private final String value;
    private final int priority;

    DomainValueSeverityEnum(String value, int priority) {
        this.value = value;
        this.priority = priority;
    }

    public String getValue() {
        return value;
    }

    public int getPriority() {
        return priority;
    }

    public static DomainValueSeverityEnum fromValue(String value) {
        for (DomainValueSeverityEnum severity : values()) {
            if (severity.value.equalsIgnoreCase(value)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown severity: " + value);
    }
}

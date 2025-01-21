package ai.shreds.domain.value_objects;

import ai.shreds.domain.exceptions.DomainException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Value object representing scheduling metadata for scan tasks.
 */
@Getter
public class DomainValueSchedulingMetadata {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final LocalDateTime startTime;

    public DomainValueSchedulingMetadata() {
        this.startTime = LocalDateTime.now();
    }

    public DomainValueSchedulingMetadata(LocalDateTime startTime) {
        this.startTime = startTime;
        validate();
    }

    public DomainValueSchedulingMetadata(String startTime) {
        try {
            this.startTime = LocalDateTime.parse(startTime, FORMATTER);
            validate();
        } catch (Exception e) {
            throw new DomainException("Invalid start time format. Expected ISO-8601 format");
        }
    }

    public void validate() {
        if (startTime == null) {
            throw new DomainException("Start time cannot be null");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new DomainException("Start time cannot be in the past");
        }
    }

    public String getStartTimeAsString() {
        return startTime.format(FORMATTER);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainValueSchedulingMetadata)) return false;
        DomainValueSchedulingMetadata that = (DomainValueSchedulingMetadata) o;
        return startTime.equals(that.startTime);
    }

    @Override
    public int hashCode() {
        return startTime.hashCode();
    }
}

package ai.shreds.shared.value_objects;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

/**
 * A value object representing scheduling metadata for scan tasks.
 * Contains information about when the scan should start.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedValueSchedulingMetadata {

    @NotNull(message = "Start time cannot be null")
    @FutureOrPresent(message = "Start time must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
}

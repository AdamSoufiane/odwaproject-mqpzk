package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO representing the completion of a scan, including findings and execution logs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedScanCompletionDTO {

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    @NotNull(message = "Vulnerability findings list cannot be null")
    private List<String> vulnerabilityFindingsList;

    @NotEmpty(message = "Scan execution logs cannot be empty")
    private List<String> scanExecutionLogs;

    @NotNull(message = "Timestamp cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}

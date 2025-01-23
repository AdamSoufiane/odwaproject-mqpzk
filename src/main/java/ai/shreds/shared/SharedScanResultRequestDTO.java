package ai.shreds.shared.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for scan result requests.
 * Contains the findings and logs from a completed security scan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedScanResultRequestDTO {

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    @NotNull(message = "Vulnerability findings list cannot be null")
    @Valid
    private List<SharedVulnerabilityDTO> vulnerabilityFindingsList;

    @NotEmpty(message = "Scan execution logs cannot be empty")
    private List<String> scanExecutionLogs;

    @NotNull(message = "Timestamp cannot be null")
    private LocalDateTime timestamp;

    /**
     * Creates a new scan result request with current timestamp.
     *
     * @param scanTaskId The ID of the scan task
     * @param findings List of vulnerability findings
     * @param logs List of execution logs
     * @return A new scan result request DTO
     */
    public static SharedScanResultRequestDTO createWithCurrentTimestamp(
            String scanTaskId,
            List<SharedVulnerabilityDTO> findings,
            List<String> logs) {
        return SharedScanResultRequestDTO.builder()
                .scanTaskId(scanTaskId)
                .vulnerabilityFindingsList(findings)
                .scanExecutionLogs(logs)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

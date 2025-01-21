package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedScanStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO representing the result of a scan operation.
 * Contains the status, result identifier, and a summary of findings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedScanResultDTO {

    @NotNull(message = "Status cannot be null")
    private SharedScanStatusEnum status;

    @NotBlank(message = "Result ID cannot be blank")
    private String resultId;

    @NotBlank(message = "Summary cannot be blank")
    private String summary;

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;
}

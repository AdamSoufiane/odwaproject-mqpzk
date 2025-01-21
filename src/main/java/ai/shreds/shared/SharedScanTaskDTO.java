package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedScanStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO representing the response or status of a scan task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedScanTaskDTO {

    @NotNull(message = "Status cannot be null")
    private SharedScanStatusEnum status;

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    @NotBlank(message = "Message cannot be blank")
    private String message;
}

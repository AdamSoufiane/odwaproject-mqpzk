package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedProtocolTypeEnum;
import ai.shreds.shared.value_objects.SharedValueSchedulingMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO representing the request for a scan task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedScanTaskRequestDTO {

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    @NotEmpty(message = "Target URLs list cannot be empty")
    private List<@NotBlank(message = "Target URL cannot be blank") String> targetUrls;

    @NotBlank(message = "Credentials cannot be blank")
    private String credentials;

    @Min(value = 1, message = "Scanning depth must be at least 1")
    private int scanningDepth;

    @NotEmpty(message = "Protocol types list cannot be empty")
    private List<@NotNull(message = "Protocol type cannot be null") SharedProtocolTypeEnum> protocolTypes;

    @NotNull(message = "Scheduling metadata cannot be null")
    private SharedValueSchedulingMetadata schedulingMetadata;
}

package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedProtocolTypeEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for scan task messages.
 * Contains all necessary information to initiate a security scan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedScanTaskMessageDTO {

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    @NotEmpty(message = "Target URLs list cannot be empty")
    private List<String> targetUrls;

    private String credentials;

    @Min(value = 1, message = "Scanning depth must be at least 1")
    private int scanningDepth;

    @NotEmpty(message = "Protocol types list cannot be empty")
    private List<SharedProtocolTypeEnum> protocolTypes;

    @NotNull(message = "Scheduling metadata cannot be null")
    private Map<String, Object> schedulingMetadata;
}

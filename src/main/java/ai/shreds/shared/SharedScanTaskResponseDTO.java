package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedScanTaskStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for scan task responses.
 * Contains the result of a scan task submission or status check.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedScanTaskResponseDTO {

    @NotNull(message = "Status cannot be null")
    private SharedScanTaskStatusEnum status;

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    @NotBlank(message = "Message cannot be blank")
    private String message;

    /**
     * Creates a success response.
     *
     * @param scanTaskId The ID of the scan task
     * @param message Additional message
     * @return A success response DTO
     */
    public static SharedScanTaskResponseDTO success(String scanTaskId, String message) {
        return SharedScanTaskResponseDTO.builder()
                .status(SharedScanTaskStatusEnum.COMPLETED)
                .scanTaskId(scanTaskId)
                .message(message)
                .build();
    }

    /**
     * Creates a failure response.
     *
     * @param scanTaskId The ID of the scan task
     * @param message Error message
     * @return A failure response DTO
     */
    public static SharedScanTaskResponseDTO failure(String scanTaskId, String message) {
        return SharedScanTaskResponseDTO.builder()
                .status(SharedScanTaskStatusEnum.FAILED)
                .scanTaskId(scanTaskId)
                .message(message)
                .build();
    }

    /**
     * Creates an invalid request response.
     *
     * @param scanTaskId The ID of the scan task
     * @param message Validation error message
     * @return An invalid response DTO
     */
    public static SharedScanTaskResponseDTO invalid(String scanTaskId, String message) {
        return SharedScanTaskResponseDTO.builder()
                .status(SharedScanTaskStatusEnum.INVALID)
                .scanTaskId(scanTaskId)
                .message(message)
                .build();
    }

    /**
     * Creates an in-progress response.
     *
     * @param scanTaskId The ID of the scan task
     * @param message Progress message
     * @return An in-progress response DTO
     */
    public static SharedScanTaskResponseDTO inProgress(String scanTaskId, String message) {
        return SharedScanTaskResponseDTO.builder()
                .status(SharedScanTaskStatusEnum.IN_PROGRESS)
                .scanTaskId(scanTaskId)
                .message(message)
                .build();
    }
}

package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedScanResultStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for scan result responses.
 * Contains the result of processing a scan result submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedScanResultResponseDTO {

    @NotNull(message = "Status cannot be null")
    private SharedScanResultStatusEnum status;

    @NotBlank(message = "Result ID cannot be blank")
    private String resultId;

    @NotBlank(message = "Summary cannot be blank")
    private String summary;

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    /**
     * Creates a successful response for processed scan results.
     *
     * @param resultId The ID of the processed result
     * @param scanTaskId The ID of the associated scan task
     * @param summary Summary of the processed results
     * @return A success response DTO
     */
    public static SharedScanResultResponseDTO processed(String resultId, String scanTaskId, String summary) {
        return SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.PROCESSED)
                .resultId(resultId)
                .scanTaskId(scanTaskId)
                .summary(summary)
                .build();
    }

    /**
     * Creates a successful response for stored scan results.
     *
     * @param resultId The ID of the stored result
     * @param scanTaskId The ID of the associated scan task
     * @param summary Summary of the stored results
     * @return A success response DTO
     */
    public static SharedScanResultResponseDTO stored(String resultId, String scanTaskId, String summary) {
        return SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.STORED)
                .resultId(resultId)
                .scanTaskId(scanTaskId)
                .summary(summary)
                .build();
    }

    /**
     * Creates an error response for invalid scan results.
     *
     * @param scanTaskId The ID of the associated scan task
     * @param errorMessage Description of the validation error
     * @return An error response DTO
     */
    public static SharedScanResultResponseDTO invalid(String scanTaskId, String errorMessage) {
        return SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.INVALID)
                .resultId("INVALID")
                .scanTaskId(scanTaskId)
                .summary(errorMessage)
                .build();
    }

    /**
     * Creates an error response for processing failures.
     *
     * @param scanTaskId The ID of the associated scan task
     * @param errorMessage Description of the error
     * @return An error response DTO
     */
    public static SharedScanResultResponseDTO error(String scanTaskId, String errorMessage) {
        return SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.ERROR)
                .resultId("ERROR")
                .scanTaskId(scanTaskId)
                .summary(errorMessage)
                .build();
    }

    /**
     * Checks if this response represents a successful operation.
     *
     * @return true if the status is PROCESSED or STORED
     */
    public boolean isSuccessful() {
        return status.isSuccessful();
    }

    /**
     * Checks if this response represents an error condition.
     *
     * @return true if the status is ERROR or INVALID
     */
    public boolean isError() {
        return status.isError();
    }
}

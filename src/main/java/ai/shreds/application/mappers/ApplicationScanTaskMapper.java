package ai.shreds.application.mappers;

import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.services.DomainScanResultEntity;
import ai.shreds.shared.dtos.SharedScanTaskMessageDTO;
import ai.shreds.shared.dtos.SharedScanTaskResponseDTO;
import ai.shreds.shared.enums.SharedScanTaskStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between scan task DTOs and domain entities.
 */
@Mapper(componentModel = "spring")
public interface ApplicationScanTaskMapper {

    /**
     * Converts a SharedScanTaskMessageDTO to a DomainEntityScanTask.
     *
     * @param dto The DTO to convert
     * @return The corresponding domain entity
     */
    @Mapping(target = "id", source = "scanTaskId")
    DomainEntityScanTask toDomainEntity(SharedScanTaskMessageDTO dto);

    /**
     * Creates a success response DTO from a scan result entity.
     *
     * @param scanTaskId The ID of the scan task
     * @param resultEntity The scan result entity
     * @return The success response DTO
     */
    @Named("toSuccessResponse")
    default SharedScanTaskResponseDTO toSuccessResponse(String scanTaskId, DomainScanResultEntity resultEntity) {
        return SharedScanTaskResponseDTO.builder()
                .status(SharedScanTaskStatusEnum.COMPLETED)
                .scanTaskId(scanTaskId)
                .message(String.format("Scan task processed successfully, vulnerabilities found: %d",
                        resultEntity != null && resultEntity.getVulnerabilities() != null 
                        ? resultEntity.getVulnerabilities().size() : 0))
                .build();
    }

    /**
     * Creates a failure response DTO.
     *
     * @param scanTaskId The ID of the scan task
     * @param errorMessage The error message
     * @return The failure response DTO
     */
    @Named("toFailureResponse")
    default SharedScanTaskResponseDTO toFailureResponse(String scanTaskId, String errorMessage) {
        return SharedScanTaskResponseDTO.builder()
                .status(SharedScanTaskStatusEnum.FAILED)
                .scanTaskId(scanTaskId)
                .message(String.format("Scan task processing failed: %s", errorMessage))
                .build();
    }

    /**
     * Creates an invalid response DTO.
     *
     * @param scanTaskId The ID of the scan task
     * @param errorMessage The validation error message
     * @return The invalid response DTO
     */
    @Named("toInvalidResponse")
    default SharedScanTaskResponseDTO toInvalidResponse(String scanTaskId, String errorMessage) {
        return SharedScanTaskResponseDTO.builder()
                .status(SharedScanTaskStatusEnum.INVALID)
                .scanTaskId(scanTaskId)
                .message(String.format("Invalid scan task: %s", errorMessage))
                .build();
    }
}

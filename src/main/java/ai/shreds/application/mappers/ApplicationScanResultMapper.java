package ai.shreds.application.mappers;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.value_objects.DomainValueVulnerability;
import ai.shreds.shared.dtos.SharedScanResultRequestDTO;
import ai.shreds.shared.dtos.SharedScanResultResponseDTO;
import ai.shreds.shared.dtos.SharedVulnerabilityDTO;
import ai.shreds.shared.enums.SharedScanResultStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * MapStruct mapper for converting between scan result DTOs and domain entities.
 */
@Mapper(componentModel = "spring")
public interface ApplicationScanResultMapper {

    /**
     * Converts a SharedScanResultRequestDTO to a DomainEntityRawScanResult.
     *
     * @param dto The DTO to convert
     * @return The corresponding domain entity
     */
    @Mapping(target = "vulnerabilityFindingsList", source = "vulnerabilityFindingsList", qualifiedByName = "mapVulnerabilities")
    DomainEntityRawScanResult toDomainEntity(SharedScanResultRequestDTO dto);

    /**
     * Maps vulnerability DTOs to domain value objects.
     *
     * @param vulnerabilities List of vulnerability DTOs
     * @return List of domain vulnerability objects
     */
    @Named("mapVulnerabilities")
    default List<DomainValueVulnerability> mapVulnerabilities(List<SharedVulnerabilityDTO> vulnerabilities) {
        if (vulnerabilities == null) {
            return null;
        }
        return vulnerabilities.stream()
                .map(dto -> DomainValueVulnerability.builder()
                        .type(dto.getType())
                        .severity(dto.getSeverity())
                        .description(dto.getDescription())
                        .location(dto.getLocation())
                        .build())
                .toList();
    }

    /**
     * Creates a success response DTO.
     *
     * @param resultId The ID of the stored result
     * @param scanTaskId The ID of the associated scan task
     * @param vulnerabilityCount The number of vulnerabilities found
     * @return The success response DTO
     */
    @Named("toSuccessResponse")
    default SharedScanResultResponseDTO toSuccessResponse(String resultId, String scanTaskId, int vulnerabilityCount) {
        return SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.STORED)
                .resultId(resultId)
                .scanTaskId(scanTaskId)
                .summary(String.format("Successfully stored scan result with %d vulnerabilities", vulnerabilityCount))
                .build();
    }

    /**
     * Creates a failure response DTO.
     *
     * @param scanTaskId The ID of the associated scan task
     * @param errorMessage The error message
     * @return The failure response DTO
     */
    @Named("toFailureResponse")
    default SharedScanResultResponseDTO toFailureResponse(String scanTaskId, String errorMessage) {
        return SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.ERROR)
                .scanTaskId(scanTaskId)
                .summary(String.format("Failed to store scan result: %s", errorMessage))
                .build();
    }

    /**
     * Creates an invalid response DTO.
     *
     * @param scanTaskId The ID of the associated scan task
     * @param errorMessage The validation error message
     * @return The invalid response DTO
     */
    @Named("toInvalidResponse")
    default SharedScanResultResponseDTO toInvalidResponse(String scanTaskId, String errorMessage) {
        return SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.INVALID)
                .scanTaskId(scanTaskId)
                .summary(String.format("Invalid scan result: %s", errorMessage))
                .build();
    }
}

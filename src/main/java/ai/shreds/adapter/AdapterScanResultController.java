package ai.shreds.adapter.primary;

import ai.shreds.adapter.exceptions.AdapterExceptionInvalidScanMessage;
import ai.shreds.application.ports.ApplicationScanResultInputPort;
import ai.shreds.shared.dtos.SharedScanResultRequestDTO;
import ai.shreds.shared.dtos.SharedScanResultResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling scan result storage requests.
 * Provides endpoints for storing and managing security scan results.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/scan")
@Validated
@Tag(name = "Scan Results", description = "API endpoints for managing security scan results")
public class AdapterScanResultController {

    private final ApplicationScanResultInputPort applicationScanResultInputPort;

    @Autowired
    public AdapterScanResultController(ApplicationScanResultInputPort applicationScanResultInputPort) {
        this.applicationScanResultInputPort = applicationScanResultInputPort;
    }

    /**
     * Stores a new scan result.
     *
     * @param params The scan result data to store
     * @return Response indicating the result of the storage operation
     */
    @Operation(summary = "Store scan result", 
              description = "Stores the results of a completed security scan")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", 
                     description = "Scan result stored successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                      schema = @Schema(implementation = SharedScanResultResponseDTO.class))),
        @ApiResponse(responseCode = "400", 
                     description = "Invalid scan result data",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                      schema = @Schema(implementation = SharedScanResultResponseDTO.class))),
        @ApiResponse(responseCode = "500", 
                     description = "Internal server error",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                      schema = @Schema(implementation = SharedScanResultResponseDTO.class)))
    })
    @PostMapping(value = "/results", 
                produces = MediaType.APPLICATION_JSON_VALUE, 
                consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SharedScanResultResponseDTO> storeScanResult(
            @Parameter(description = "Scan result data", required = true)
            @Valid @RequestBody SharedScanResultRequestDTO params) {
        
        log.info("Received scan result storage request for scan task ID: {}", params.getScanTaskId());
        
        try {
            validateScanResult(params);
            log.debug("Scan result validation passed for task ID: {}", params.getScanTaskId());

            SharedScanResultResponseDTO response = applicationScanResultInputPort.storeRawScanResult(params);
            log.info("Successfully stored scan result for task ID: {}", params.getScanTaskId());

            return ResponseEntity.status(201).body(response);

        } catch (AdapterExceptionInvalidScanMessage e) {
            log.error("Invalid scan result data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error storing scan result", e);
            throw e;
        }
    }

    /**
     * Validates the scan result data.
     * Performs additional validation beyond basic field validation.
     *
     * @param params The scan result data to validate
     * @throws AdapterExceptionInvalidScanMessage if validation fails
     */
    private void validateScanResult(SharedScanResultRequestDTO params) {
        if (params == null) {
            throw new AdapterExceptionInvalidScanMessage("Scan result data cannot be null");
        }
        if (params.getScanTaskId() == null || params.getScanTaskId().trim().isEmpty()) {
            throw new AdapterExceptionInvalidScanMessage("Scan task ID cannot be empty");
        }
        if (params.getVulnerabilityFindingsList() == null) {
            throw new AdapterExceptionInvalidScanMessage("Vulnerability findings list cannot be null");
        }
        if (params.getScanExecutionLogs() == null || params.getScanExecutionLogs().isEmpty()) {
            throw new AdapterExceptionInvalidScanMessage("Scan execution logs cannot be empty");
        }
        if (params.getTimestamp() == null) {
            throw new AdapterExceptionInvalidScanMessage("Timestamp cannot be null");
        }
    }
}

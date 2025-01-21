package ai.shreds.adapter.primary;

import ai.shreds.adapter.exceptions.AdapterException;
import ai.shreds.shared.dtos.SharedScanCompletionDTO;
import ai.shreds.shared.dtos.SharedScanResultDTO;
import ai.shreds.application.ports.ApplicationScanResultInputPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/scan")
public class AdapterScanResultController {

    private final ApplicationScanResultInputPort applicationScanResultInputPort;

    public AdapterScanResultController(ApplicationScanResultInputPort applicationScanResultInputPort) {
        this.applicationScanResultInputPort = applicationScanResultInputPort;
    }

    @PostMapping("/results")
    public ResponseEntity<SharedScanResultDTO> submitScanResults(
            @Valid @RequestBody SharedScanCompletionDTO request) {
        log.info("Received scan completion request for task ID: {}", request.getScanTaskId());
        
        try {
            SharedScanResultDTO result = applicationScanResultInputPort.storeScanResult(request);
            log.info("Successfully stored scan results for task ID: {}", result.getScanTaskId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error storing scan results: {}", e.getMessage(), e);
            throw new AdapterException("Failed to store scan results", "SCAN_RESULT_STORAGE_ERROR", e);
        }
    }

    @ExceptionHandler(AdapterException.class)
    public ResponseEntity<ErrorResponse> handleAdapterException(AdapterException ex) {
        log.error("Adapter exception occurred: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private static class ErrorResponse {
        private final String errorCode;
        private final String message;

        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getMessage() {
            return message;
        }
    }
}

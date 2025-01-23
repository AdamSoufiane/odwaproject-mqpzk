package ai.shreds.adapter.exceptions;

import ai.shreds.shared.dtos.SharedScanResultResponseDTO;
import ai.shreds.shared.dtos.SharedScanTaskResponseDTO;
import ai.shreds.shared.enums.SharedScanResultStatusEnum;
import ai.shreds.shared.enums.SharedScanTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for the adapter layer.
 * Handles exceptions and converts them to appropriate API responses.
 */
@Slf4j
@RestControllerAdvice
public class AdapterExceptionHandler {

    /**
     * Handles validation exceptions for scan messages.
     *
     * @param ex The invalid scan message exception
     * @return Error response with validation details
     */
    @ExceptionHandler(AdapterExceptionInvalidScanMessage.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<SharedScanTaskResponseDTO> handleInvalidScanMessage(AdapterExceptionInvalidScanMessage ex) {
        log.error("Invalid scan message: {}", ex.getMessage());
        SharedScanTaskResponseDTO response = SharedScanTaskResponseDTO.builder()
                .status(SharedScanTaskStatusEnum.INVALID)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles validation exceptions from request body validation.
     *
     * @param ex The method argument validation exception
     * @return Error response with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<SharedScanResultResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("Validation failed: {}", errors);

        SharedScanResultResponseDTO response = SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.INVALID)
                .summary("Validation failed: " + errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles unexpected exceptions.
     *
     * @param ex The unexpected exception
     * @return Error response with generic error message
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<SharedScanResultResponseDTO> handleUnexpectedExceptions(Exception ex) {
        log.error("Unexpected error occurred", ex);

        SharedScanResultResponseDTO response = SharedScanResultResponseDTO.builder()
                .status(SharedScanResultStatusEnum.ERROR)
                .summary("An unexpected error occurred: " + ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

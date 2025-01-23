package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedScanTaskMessageDTO;
import ai.shreds.shared.dtos.SharedScanTaskResponseDTO;

/**
 * Input port for processing security scan tasks.
 * This port defines the interface for the application layer to receive and process scan tasks.
 */
public interface ApplicationScanTaskInputPort {

    /**
     * Processes a security scan task.
     * Validates the task, initiates the scanning process, and returns the result.
     *
     * @param scanTask The scan task to process, containing target URLs, credentials, and scanning configuration
     * @return Response indicating the result of task processing
     * @throws ai.shreds.application.exceptions.ApplicationExceptionValidation if the task fails validation
     * @throws ai.shreds.application.exceptions.ApplicationExceptionInvalidScan if the scan configuration is invalid
     */
    SharedScanTaskResponseDTO processScanTask(SharedScanTaskMessageDTO scanTask);
}

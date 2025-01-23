package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedScanResultRequestDTO;
import ai.shreds.shared.dtos.SharedScanResultResponseDTO;

/**
 * Input port for storing raw security scan results.
 * This port defines the interface for the application layer to receive and store scan results.
 */
public interface ApplicationScanResultInputPort {

    /**
     * Stores raw scan results from a completed security scan.
     * Validates the results and persists them in the repository.
     *
     * @param scanResult The scan result to store, containing findings and execution logs
     * @return Response indicating the result of the storage operation
     * @throws ai.shreds.application.exceptions.ApplicationExceptionValidation if the result fails validation
     * @throws ai.shreds.application.exceptions.ApplicationExceptionInvalidScan if the scan result is invalid
     */
    SharedScanResultResponseDTO storeRawScanResult(SharedScanResultRequestDTO scanResult);
}

package ai.shreds.domain.ports;

import ai.shreds.domain.services.DomainScanResultEntity;
import ai.shreds.domain.services.DomainScanTaskEntity;

/**
 * Port interface for security scan execution operations.
 * Defines the contract for executing and managing security scans.
 */
public interface DomainScanTaskPort {

    /**
     * Executes a security scan.
     *
     * @param scanTask The scan task to execute
     * @return The scan results
     * @throws ai.shreds.domain.exceptions.DomainExceptionScanValidation if validation fails
     * @throws ai.shreds.domain.exceptions.DomainExceptionScanExecution if execution fails
     */
    DomainScanResultEntity executeScan(DomainScanTaskEntity scanTask);

    /**
     * Validates a scan task before execution.
     *
     * @param scanTask The scan task to validate
     * @return true if the task is valid
     * @throws ai.shreds.domain.exceptions.DomainExceptionScanValidation if validation fails
     */
    default boolean validateScanTask(DomainScanTaskEntity scanTask) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Pauses an ongoing scan.
     *
     * @param scanTaskId The ID of the scan task to pause
     * @return true if the scan was successfully paused
     */
    default boolean pauseScan(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Resumes a paused scan.
     *
     * @param scanTaskId The ID of the scan task to resume
     * @return true if the scan was successfully resumed
     */
    default boolean resumeScan(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Cancels an ongoing scan.
     *
     * @param scanTaskId The ID of the scan task to cancel
     * @return true if the scan was successfully cancelled
     */
    default boolean cancelScan(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Gets the current progress of a scan.
     *
     * @param scanTaskId The ID of the scan task
     * @return Progress percentage (0-100)
     */
    default int getScanProgress(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Gets the current status of a scan.
     *
     * @param scanTaskId The ID of the scan task
     * @return Current scan status
     */
    default String getScanStatus(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Checks if a scan is currently running.
     *
     * @param scanTaskId The ID of the scan task
     * @return true if the scan is running
     */
    default boolean isScanRunning(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Gets the estimated time remaining for a scan.
     *
     * @param scanTaskId The ID of the scan task
     * @return Estimated minutes remaining, or -1 if unknown
     */
    default int getEstimatedTimeRemaining(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }
}

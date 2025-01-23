package ai.shreds.domain.ports;

import ai.shreds.domain.services.DomainScanResultEntity;
import ai.shreds.domain.value_objects.DomainVulnerabilitySeverity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Port interface for scan result persistence operations.
 * Defines the contract for storing and retrieving processed scan results.
 */
public interface DomainScanRepositoryPort {

    /**
     * Saves a processed scan result.
     *
     * @param scanResult The scan result to save
     * @throws IllegalArgumentException if the result is invalid
     */
    void saveScanResult(DomainScanResultEntity scanResult);

    /**
     * Finds a scan result by its ID.
     *
     * @param resultId The ID of the result to find
     * @return The found scan result or null if not found
     */
    default DomainScanResultEntity findResultById(String resultId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Finds all results for a specific scan task.
     *
     * @param scanTaskId The ID of the scan task
     * @return List of scan results for the task
     */
    default List<DomainScanResultEntity> findResultsByScanTaskId(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Finds results by vulnerability severity.
     *
     * @param severity The severity level to search for
     * @return List of scan results containing vulnerabilities of the specified severity
     */
    default List<DomainScanResultEntity> findResultsBySeverity(DomainVulnerabilitySeverity severity) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Finds results within a specific time range.
     *
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return List of scan results within the specified time range
     */
    default List<DomainScanResultEntity> findResultsByTimeRange(LocalDateTime startTime, 
                                                               LocalDateTime endTime) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Updates the execution logs of a scan result.
     *
     * @param resultId The ID of the result to update
     * @param logs The new execution logs
     * @return true if the update was successful
     */
    default boolean updateExecutionLogs(String resultId, List<String> logs) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Adds a vulnerability to an existing scan result.
     *
     * @param resultId The ID of the result to update
     * @param vulnerability The vulnerability to add
     * @return true if the update was successful
     */
    default boolean addVulnerability(String resultId, DomainVulnerabilitySeverity vulnerability) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Deletes a scan result.
     *
     * @param resultId The ID of the result to delete
     * @return true if the deletion was successful
     */
    default boolean deleteResult(String resultId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Deletes all results for a specific scan task.
     *
     * @param scanTaskId The ID of the scan task
     * @return Number of results deleted
     */
    default int deleteResultsByScanTaskId(String scanTaskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Counts the number of results for a specific scan task.
     *
     * @param scanTaskId The ID of the scan task
     * @return Number of results found
     */
    default long countResultsByScanTaskId(String scanTaskId) {
        List<DomainScanResultEntity> results = findResultsByScanTaskId(scanTaskId);
        return results != null ? results.size() : 0;
    }

    /**
     * Checks if a scan result exists.
     *
     * @param resultId The ID of the result to check
     * @return true if the result exists
     */
    default boolean existsById(String resultId) {
        return findResultById(resultId) != null;
    }
}

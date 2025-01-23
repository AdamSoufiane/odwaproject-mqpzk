package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityScanTask;

import java.util.List;

/**
 * Port interface for scan task persistence operations.
 * Defines the contract for storing and retrieving security scan tasks.
 */
public interface DomainPortScanTaskRepository {

    /**
     * Saves a scan task.
     *
     * @param task The scan task to save
     * @throws IllegalArgumentException if the task is invalid
     */
    void saveScanTask(DomainEntityScanTask task);

    /**
     * Finds a scan task by its ID.
     *
     * @param id The ID of the scan task to find
     * @return The found scan task or null if not found
     */
    DomainEntityScanTask findScanTaskById(String id);

    /**
     * Finds all scan tasks for a specific target URL.
     *
     * @param targetUrl The target URL to search for
     * @return List of scan tasks targeting the specified URL
     */
    default List<DomainEntityScanTask> findScanTasksByTargetUrl(String targetUrl) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Finds all pending scan tasks.
     *
     * @return List of scan tasks that haven't been executed yet
     */
    default List<DomainEntityScanTask> findPendingScanTasks() {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Updates the status of a scan task.
     *
     * @param taskId The ID of the task to update
     * @param status The new status
     * @return true if the update was successful
     */
    default boolean updateScanTaskStatus(String taskId, String status) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Deletes a scan task.
     *
     * @param taskId The ID of the task to delete
     * @return true if the deletion was successful
     */
    default boolean deleteScanTask(String taskId) {
        throw new UnsupportedOperationException("Operation not implemented");
    }

    /**
     * Checks if a scan task exists.
     *
     * @param taskId The ID of the task to check
     * @return true if the task exists
     */
    default boolean existsById(String taskId) {
        return findScanTaskById(taskId) != null;
    }
}

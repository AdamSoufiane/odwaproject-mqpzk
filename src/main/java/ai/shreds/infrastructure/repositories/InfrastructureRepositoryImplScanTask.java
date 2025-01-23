package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.ports.DomainPortScanTaskRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionMongo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the domain repository port for scan tasks.
 * Provides persistence operations for scan tasks using MongoDB.
 */
@Slf4j
@Repository
public class InfrastructureRepositoryImplScanTask implements DomainPortScanTaskRepository {

    private final InfrastructureMongoScanTaskRepositoryImpl mongoRepository;

    public InfrastructureRepositoryImplScanTask(InfrastructureMongoScanTaskRepositoryImpl mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    @Transactional
    public void saveScanTask(DomainEntityScanTask task) {
        try {
            log.debug("Saving scan task with ID: {}", task.getId());
            validateScanTask(task);
            mongoRepository.insertScanTask(task);
            log.info("Successfully saved scan task with ID: {}", task.getId());

        } catch (Exception e) {
            log.error("Failed to save scan task with ID: {}", task.getId(), e);
            throw new InfrastructureExceptionMongo(
                    String.format("Failed to save scan task with ID %s: %s", 
                            task.getId(), e.getMessage()), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DomainEntityScanTask findScanTaskById(String id) {
        try {
            log.debug("Finding scan task with ID: {}", id);
            DomainEntityScanTask task = mongoRepository.findScanTaskById(id);

            if (task == null) {
                log.warn("Scan task not found with ID: {}", id);
                return null;
            }

            log.debug("Found scan task with ID: {}", id);
            return task;

        } catch (Exception e) {
            log.error("Failed to find scan task with ID: {}", id, e);
            throw new InfrastructureExceptionMongo(
                    String.format("Failed to find scan task with ID %s: %s", 
                            id, e.getMessage()), e);
        }
    }

    /**
     * Validates a scan task before saving.
     *
     * @param task The scan task to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateScanTask(DomainEntityScanTask task) {
        if (task == null) {
            throw new IllegalArgumentException("Scan task cannot be null");
        }

        if (task.getId() == null || task.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Scan task ID cannot be null or empty");
        }

        if (task.getTargetUrls() == null || task.getTargetUrls().isEmpty()) {
            throw new IllegalArgumentException("Target URLs cannot be null or empty");
        }

        if (task.getProtocolTypes() == null || task.getProtocolTypes().isEmpty()) {
            throw new IllegalArgumentException("Protocol types cannot be null or empty");
        }

        if (task.getScanningDepth() <= 0) {
            throw new IllegalArgumentException("Scanning depth must be greater than 0");
        }

        if (task.getSchedulingMetadata() == null) {
            throw new IllegalArgumentException("Scheduling metadata cannot be null");
        }
    }
}

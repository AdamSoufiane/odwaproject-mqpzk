package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.ports.DomainPortRawScanResultRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionMongo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of the domain repository port for raw scan results.
 * Provides persistence operations for scan results using MongoDB.
 */
@Slf4j
@Repository
public class InfrastructureRepositoryImplRawScanResult implements DomainPortRawScanResultRepository {

    private final InfrastructureMongoRawScanResultRepositoryImpl mongoRepository;

    public InfrastructureRepositoryImplRawScanResult(InfrastructureMongoRawScanResultRepositoryImpl mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    @Transactional
    public void saveRawScanResult(DomainEntityRawScanResult result) {
        try {
            log.debug("Saving raw scan result for task ID: {}", result.getScanTaskId());
            validateScanResult(result);
            mongoRepository.insertRawScanResult(result);
            log.info("Successfully saved raw scan result with ID: {} for task ID: {}", 
                    result.getId(), result.getScanTaskId());

        } catch (Exception e) {
            log.error("Failed to save raw scan result for task ID: {}", result.getScanTaskId(), e);
            throw new InfrastructureExceptionMongo(
                    String.format("Failed to save raw scan result for task ID %s: %s", 
                            result.getScanTaskId(), e.getMessage()),
                    "raw_scan_results", "SAVE", "SAVE_ERROR", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEntityRawScanResult> findResultsByScanTaskId(String scanTaskId) {
        try {
            log.debug("Finding raw scan results for task ID: {}", scanTaskId);

            if (scanTaskId == null || scanTaskId.trim().isEmpty()) {
                log.warn("Attempted to find results with null or empty task ID");
                return Collections.emptyList();
            }

            List<DomainEntityRawScanResult> results = mongoRepository.findRawResultsByScanTaskId(scanTaskId);
            log.debug("Found {} raw scan results for task ID: {}", results.size(), scanTaskId);

            return results;

        } catch (Exception e) {
            log.error("Failed to find raw scan results for task ID: {}", scanTaskId, e);
            throw new InfrastructureExceptionMongo(
                    String.format("Failed to find raw scan results for task ID %s: %s", 
                            scanTaskId, e.getMessage()),
                    "raw_scan_results", "FIND", "FIND_ERROR", e);
        }
    }

    /**
     * Validates a raw scan result before saving.
     *
     * @param result The scan result to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateScanResult(DomainEntityRawScanResult result) {
        if (result == null) {
            throw new IllegalArgumentException("Scan result cannot be null");
        }

        if (result.getScanTaskId() == null || result.getScanTaskId().trim().isEmpty()) {
            throw new IllegalArgumentException("Scan task ID cannot be null or empty");
        }

        if (result.getVulnerabilityFindingsList() == null) {
            throw new IllegalArgumentException("Vulnerability findings list cannot be null");
        }

        if (result.getScanExecutionLogs() == null || result.getScanExecutionLogs().isEmpty()) {
            throw new IllegalArgumentException("Scan execution logs cannot be empty");
        }

        if (result.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        // Validate each vulnerability
        result.getVulnerabilityFindingsList().forEach(vulnerability -> {
            if (vulnerability == null) {
                throw new IllegalArgumentException("Vulnerability cannot be null");
            }
            vulnerability.validate();
        });
    }
}

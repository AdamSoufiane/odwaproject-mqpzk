package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.exceptions.DomainErrorCode;
import ai.shreds.domain.ports.DomainPortRawScanResultRepository;
import ai.shreds.infrastructure.repositories.documents.RawScanResultDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InfrastructureMongoRawScanResultRepositoryImpl implements DomainPortRawScanResultRepository {

    private final MongoTemplate mongoTemplate;

    public InfrastructureMongoRawScanResultRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void saveRawScanResult(DomainEntityRawScanResult result) {
        log.debug("Saving raw scan result with ID: {}", result.getId());
        try {
            RawScanResultDocument document = mapToDocument(result);
            mongoTemplate.save(document);
            log.info("Successfully saved raw scan result with ID: {}", result.getId());
        } catch (DataAccessException e) {
            log.error("Failed to save raw scan result: {}", e.getMessage(), e);
            throw new DomainException("Failed to save raw scan result", DomainErrorCode.RESULT_SAVE_ERROR, e);
        }
    }

    @Override
    public List<DomainEntityRawScanResult> findByScanTaskId(String scanTaskId) {
        log.debug("Finding raw scan results for task ID: {}", scanTaskId);
        try {
            Query query = new Query(Criteria.where("scanTaskId").is(scanTaskId));
            List<RawScanResultDocument> documents = mongoTemplate.find(query, RawScanResultDocument.class);

            log.info("Found {} raw scan results for task ID: {}", documents.size(), scanTaskId);
            return documents.stream()
                    .map(this::mapToDomainEntity)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("Failed to retrieve raw scan results: {}", e.getMessage(), e);
            throw new DomainException("Failed to retrieve raw scan results", DomainErrorCode.RESULT_RETRIEVAL_ERROR, e);
        }
    }

    private DomainEntityRawScanResult mapToDomainEntity(RawScanResultDocument document) {
        DomainEntityRawScanResult entity = new DomainEntityRawScanResult();
        entity.setId(document.getId());
        entity.setScanTaskId(document.getScanTaskId());
        entity.setVulnerabilityFindingsList(document.getVulnerabilityFindingsList());
        entity.setTimestamp(document.getTimestamp());
        entity.setScanExecutionLogs(document.getScanExecutionLogs());
        return entity;
    }

    private RawScanResultDocument mapToDocument(DomainEntityRawScanResult entity) {
        RawScanResultDocument document = new RawScanResultDocument();
        document.setId(entity.getId());
        document.setScanTaskId(entity.getScanTaskId());
        document.setVulnerabilityFindingsList(entity.getVulnerabilityFindingsList());
        document.setTimestamp(entity.getTimestamp());
        document.setScanExecutionLogs(entity.getScanExecutionLogs());
        document.setSeverity(entity.getFindings().getSeverity().getValue());
        return document;
    }
}

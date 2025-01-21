package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.exceptions.DomainErrorCode;
import ai.shreds.domain.ports.DomainPortScanTaskRepository;
import ai.shreds.domain.value_objects.DomainValueSchedulingMetadata;
import ai.shreds.infrastructure.repositories.documents.ScanTaskDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class InfrastructureMongoScanTaskRepositoryImpl implements DomainPortScanTaskRepository {

    private final MongoTemplate mongoTemplate;

    public InfrastructureMongoScanTaskRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void saveScanTask(DomainEntityScanTask task) {
        log.debug("Saving scan task with ID: {}", task.getId());
        try {
            ScanTaskDocument document = mapToDocument(task);
            mongoTemplate.save(document);
            log.info("Successfully saved scan task with ID: {}", task.getId());
        } catch (DataAccessException e) {
            log.error("Failed to save scan task: {}", e.getMessage(), e);
            throw new DomainException("Failed to save scan task", DomainErrorCode.TASK_SAVE_ERROR, e);
        }
    }

    @Override
    public DomainEntityScanTask findById(String scanTaskId) {
        log.debug("Finding scan task by ID: {}", scanTaskId);
        try {
            Query query = new Query(Criteria.where("id").is(scanTaskId));
            ScanTaskDocument document = mongoTemplate.findOne(query, ScanTaskDocument.class);

            if (document == null) {
                log.warn("No scan task found with ID: {}", scanTaskId);
                return null;
            }

            log.info("Successfully retrieved scan task with ID: {}", scanTaskId);
            return mapToDomainEntity(document);
        } catch (DataAccessException e) {
            log.error("Failed to retrieve scan task: {}", e.getMessage(), e);
            throw new DomainException("Failed to retrieve scan task", DomainErrorCode.TASK_RETRIEVAL_ERROR, e);
        }
    }

    private DomainEntityScanTask mapToDomainEntity(ScanTaskDocument document) {
        DomainEntityScanTask entity = new DomainEntityScanTask();
        entity.setId(document.getId());
        entity.setTargetUrls(document.getTargetUrls());
        entity.setCredentials(document.getCredentials());
        entity.setScanningDepth(document.getScanningDepth());
        entity.setProtocolTypes(document.getProtocolTypes());
        entity.setSchedulingMetadata(new DomainValueSchedulingMetadata(document.getStartTime()));
        return entity;
    }

    private ScanTaskDocument mapToDocument(DomainEntityScanTask entity) {
        ScanTaskDocument document = new ScanTaskDocument();
        document.setId(entity.getId());
        document.setTargetUrls(entity.getTargetUrls());
        document.setCredentials(entity.getCredentials());
        document.setScanningDepth(entity.getScanningDepth());
        document.setProtocolTypes(entity.getProtocolTypes());
        document.setStartTime(entity.getSchedulingMetadata().getStartTimeAsString());
        return document;
    }
}

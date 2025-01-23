package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainScanRepositoryPort;
import ai.shreds.domain.services.DomainScanResultEntity;
import ai.shreds.domain.value_objects.DomainValueVulnerability;
import ai.shreds.domain.value_objects.DomainVulnerabilitySeverity;
import ai.shreds.infrastructure.config.InfrastructureMongoConfig;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionMongo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the domain scan repository port.
 * Handles persistence of processed scan results using MongoDB.
 */
@Slf4j
@Component
public class InfrastructureScanRepositoryPortImpl implements DomainScanRepositoryPort {

    private static final String COLLECTION_NAME = "processed_scan_results";
    private final InfrastructureMongoConfig mongoConfig;

    public InfrastructureScanRepositoryPortImpl(InfrastructureMongoConfig mongoConfig) {
        this.mongoConfig = mongoConfig;
    }

    @PostConstruct
    public void init() {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            // Create indexes
            collection.createIndex(Indexes.ascending("resultId"), 
                    new IndexOptions().unique(true));
            collection.createIndex(Indexes.ascending("scanTaskId"));
            collection.createIndex(Indexes.ascending("timestamp"));

            log.info("MongoDB collection and indexes initialized for {}", COLLECTION_NAME);
        } catch (Exception e) {
            log.error("Failed to initialize MongoDB collection", e);
            throw new InfrastructureExceptionMongo("Failed to initialize MongoDB collection", e);
        }
    }

    @Override
    public void saveScanResult(DomainScanResultEntity scanResult) {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            Document doc = convertToDocument(scanResult);
            Bson filter = Filters.eq("resultId", scanResult.getResultId());

            Document existingDoc = collection.find(filter).first();
            if (existingDoc == null) {
                collection.insertOne(doc);
                log.debug("Inserted new processed scan result with ID: {}", scanResult.getResultId());
            } else {
                collection.replaceOne(filter, doc);
                log.debug("Updated existing processed scan result with ID: {}", scanResult.getResultId());
            }

        } catch (Exception e) {
            log.error("Failed to save processed scan result", e);
            throw new InfrastructureExceptionMongo(
                    "Failed to save processed scan result", 
                    COLLECTION_NAME, "SAVE", "SAVE_ERROR", e);
        }
    }

    @Override
    public DomainScanResultEntity findResultById(String resultId) {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            Document doc = collection.find(Filters.eq("resultId", resultId)).first();
            return doc != null ? convertToEntity(doc) : null;

        } catch (Exception e) {
            log.error("Failed to find processed scan result with ID: {}", resultId, e);
            throw new InfrastructureExceptionMongo(
                    String.format("Failed to find processed scan result with ID %s", resultId),
                    COLLECTION_NAME, "FIND", "FIND_ERROR", e);
        }
    }

    @Override
    public List<DomainScanResultEntity> findResultsByScanTaskId(String scanTaskId) {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            List<DomainScanResultEntity> results = new ArrayList<>();
            collection.find(Filters.eq("scanTaskId", scanTaskId))
                    .forEach(doc -> results.add(convertToEntity(doc)));
            return results;

        } catch (Exception e) {
            log.error("Failed to find processed scan results for task ID: {}", scanTaskId, e);
            throw new InfrastructureExceptionMongo(
                    String.format("Failed to find processed scan results for task ID %s", scanTaskId),
                    COLLECTION_NAME, "FIND", "FIND_ERROR", e);
        }
    }

    private Document convertToDocument(DomainScanResultEntity entity) {
        List<Document> vulnerabilitiesDoc = entity.getVulnerabilities().stream()
                .map(v -> new Document()
                        .append("type", v.getType())
                        .append("severity", v.getSeverity().getValue())
                        .append("description", v.getDescription())
                        .append("location", v.getLocation()))
                .collect(Collectors.toList());

        return new Document()
                .append("resultId", entity.getResultId())
                .append("scanTaskId", entity.getScanTaskId())
                .append("vulnerabilities", vulnerabilitiesDoc)
                .append("timestamp", entity.getTimestamp().toString())
                .append("executionLogs", entity.getExecutionLogs());
    }

    private DomainScanResultEntity convertToEntity(Document doc) {
        List<Document> vulDocs = doc.getList("vulnerabilities", Document.class);
        List<DomainValueVulnerability> vulnerabilities = vulDocs.stream()
                .map(vulDoc -> DomainValueVulnerability.builder()
                        .type(vulDoc.getString("type"))
                        .severity(DomainVulnerabilitySeverity.fromString(vulDoc.getString("severity")))
                        .description(vulDoc.getString("description"))
                        .location(vulDoc.getString("location"))
                        .build())
                .collect(Collectors.toList());

        return DomainScanResultEntity.builder()
                .resultId(doc.getString("resultId"))
                .scanTaskId(doc.getString("scanTaskId"))
                .vulnerabilities(vulnerabilities)
                .timestamp(LocalDateTime.parse(doc.getString("timestamp")))
                .executionLogs(doc.getList("executionLogs", String.class))
                .build();
    }
}

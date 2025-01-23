package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
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
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB implementation for raw scan result persistence.
 * Handles direct interactions with MongoDB for storing and retrieving scan results.
 */
@Slf4j
@Repository
public class InfrastructureMongoRawScanResultRepositoryImpl {

    private static final String COLLECTION_NAME = "raw_scan_results";
    private final InfrastructureMongoConfig mongoConfig;

    public InfrastructureMongoRawScanResultRepositoryImpl(InfrastructureMongoConfig mongoConfig) {
        this.mongoConfig = mongoConfig;
    }

    /**
     * Initializes the MongoDB collection and indexes.
     */
    @PostConstruct
    public void init() {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            // Create indexes
            collection.createIndex(Indexes.ascending("id"),
                    new IndexOptions().unique(true));
            collection.createIndex(Indexes.ascending("scanTaskId"));
            collection.createIndex(Indexes.ascending("timestamp"));

            log.info("MongoDB collection and indexes initialized for {}", COLLECTION_NAME);
        } catch (Exception e) {
            log.error("Failed to initialize MongoDB collection", e);
            throw new InfrastructureExceptionMongo("Failed to initialize MongoDB collection", e);
        }
    }

    /**
     * Inserts or updates a raw scan result document.
     *
     * @param document The scan result to persist
     * @throws InfrastructureExceptionMongo if the operation fails
     */
    public void insertRawScanResult(DomainEntityRawScanResult document) {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            Document doc = convertToDocument(document);
            Bson filter = Filters.eq("id", document.getId());

            try {
                Document existingDoc = collection.find(filter).first();
                if (existingDoc == null) {
                    collection.insertOne(doc);
                    log.debug("Inserted new raw scan result with ID: {}", document.getId());
                } else {
                    collection.replaceOne(filter, doc);
                    log.debug("Updated existing raw scan result with ID: {}", document.getId());
                }
            } catch (Exception e) {
                throw new InfrastructureExceptionMongo(
                        String.format("Failed to save raw scan result with ID %s", document.getId()),
                        COLLECTION_NAME, "SAVE", "SAVE_ERROR", e);
            }
        }
    }

    /**
     * Finds raw scan results by scan task ID.
     *
     * @param scanTaskId The ID of the scan task
     * @return List of found scan results
     * @throws InfrastructureExceptionMongo if the operation fails
     */
    public List<DomainEntityRawScanResult> findRawResultsByScanTaskId(String scanTaskId) {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            List<DomainEntityRawScanResult> results = new ArrayList<>();
            Bson filter = Filters.eq("scanTaskId", scanTaskId);

            collection.find(filter).forEach(doc -> results.add(convertToEntity(doc)));
            log.debug("Found {} raw scan results for task ID: {}", results.size(), scanTaskId);

            return results;
        } catch (Exception e) {
            throw new InfrastructureExceptionMongo(
                    String.format("Failed to find raw scan results for task ID %s", scanTaskId),
                    COLLECTION_NAME, "FIND", "FIND_ERROR", e);
        }
    }

    /**
     * Converts a domain entity to a MongoDB document.
     *
     * @param entity The entity to convert
     * @return The MongoDB document
     */
    private Document convertToDocument(DomainEntityRawScanResult entity) {
        List<Document> vulnerabilitiesDocList = new ArrayList<>();
        if (entity.getVulnerabilityFindingsList() != null) {
            for (DomainValueVulnerability vul : entity.getVulnerabilityFindingsList()) {
                Document vulDoc = new Document()
                        .append("type", vul.getType())
                        .append("severity", vul.getSeverity().getValue())
                        .append("description", vul.getDescription())
                        .append("location", vul.getLocation());
                vulnerabilitiesDocList.add(vulDoc);
            }
        }

        return new Document()
                .append("id", entity.getId())
                .append("scanTaskId", entity.getScanTaskId())
                .append("vulnerabilityFindingsList", vulnerabilitiesDocList)
                .append("timestamp", entity.getTimestamp().toString())
                .append("scanExecutionLogs", entity.getScanExecutionLogs());
    }

    /**
     * Converts a MongoDB document to a domain entity.
     *
     * @param doc The document to convert
     * @return The domain entity
     */
    private DomainEntityRawScanResult convertToEntity(Document doc) {
        List<Document> vulDocs = doc.getList("vulnerabilityFindingsList", Document.class);
        List<DomainValueVulnerability> vulnerabilities = new ArrayList<>();

        if (vulDocs != null) {
            for (Document vulDoc : vulDocs) {
                vulnerabilities.add(DomainValueVulnerability.builder()
                        .type(vulDoc.getString("type"))
                        .severity(DomainVulnerabilitySeverity.fromString(vulDoc.getString("severity")))
                        .description(vulDoc.getString("description"))
                        .location(vulDoc.getString("location"))
                        .build());
            }
        }

        return DomainEntityRawScanResult.builder()
                .id(doc.getString("id"))
                .scanTaskId(doc.getString("scanTaskId"))
                .vulnerabilityFindingsList(vulnerabilities)
                .timestamp(LocalDateTime.parse(doc.getString("timestamp")))
                .scanExecutionLogs(doc.getList("scanExecutionLogs", String.class))
                .build();
    }
}

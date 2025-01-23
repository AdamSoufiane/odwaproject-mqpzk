package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.infrastructure.config.InfrastructureMongoConfig;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionMongo;
import ai.shreds.shared.enums.SharedProtocolTypeEnum;
import com.mongodb.MongoException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MongoDB implementation for scan task persistence.
 * Handles direct interactions with MongoDB for storing and retrieving scan tasks.
 */
@Slf4j
@Repository
public class InfrastructureMongoScanTaskRepositoryImpl {

    private static final String COLLECTION_NAME = "scan_tasks";
    private final InfrastructureMongoConfig mongoConfig;

    public InfrastructureMongoScanTaskRepositoryImpl(InfrastructureMongoConfig mongoConfig) {
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
            collection.createIndex(Indexes.ascending("targetUrls"));

            log.info("MongoDB collection and indexes initialized for {}", COLLECTION_NAME);
        } catch (Exception e) {
            log.error("Failed to initialize MongoDB collection", e);
            throw new InfrastructureExceptionMongo("Failed to initialize MongoDB collection", e);
        }
    }

    /**
     * Inserts or updates a scan task document.
     *
     * @param document The scan task to persist
     * @throws InfrastructureExceptionMongo if the operation fails
     */
    public void insertScanTask(DomainEntityScanTask document) {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            Document doc = convertToDocument(document);
            Bson filter = Filters.eq("id", document.getId());

            try {
                Document existingDoc = collection.find(filter).first();
                if (existingDoc == null) {
                    collection.insertOne(doc);
                    log.debug("Inserted new scan task with ID: {}", document.getId());
                } else {
                    collection.replaceOne(filter, doc);
                    log.debug("Updated existing scan task with ID: {}", document.getId());
                }
            } catch (MongoException e) {
                throw new InfrastructureExceptionMongo(
                        String.format("Failed to save scan task with ID %s", document.getId()), e);
            }
        }
    }

    /**
     * Finds a scan task by its ID.
     *
     * @param id The ID of the scan task to find
     * @return The found scan task or null if not found
     * @throws InfrastructureExceptionMongo if the operation fails
     */
    public DomainEntityScanTask findScanTaskById(String id) {
        try (MongoClient client = mongoConfig.getMongoClient()) {
            MongoDatabase db = client.getDatabase(mongoConfig.getDatabaseName());
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            Bson filter = Filters.eq("id", id);
            Document doc = collection.find(filter).first();

            if (doc == null) {
                log.debug("No scan task found with ID: {}", id);
                return null;
            }

            return convertToEntity(doc);
        } catch (Exception e) {
            throw new InfrastructureExceptionMongo(
                    String.format("Failed to find scan task with ID %s", id), e);
        }
    }

    /**
     * Converts a domain entity to a MongoDB document.
     *
     * @param entity The entity to convert
     * @return The MongoDB document
     */
    private Document convertToDocument(DomainEntityScanTask entity) {
        return new Document()
                .append("id", entity.getId())
                .append("targetUrls", entity.getTargetUrls())
                .append("credentials", entity.getCredentials())
                .append("scanningDepth", entity.getScanningDepth())
                .append("protocolTypes", entity.getProtocolTypes().stream()
                        .map(SharedProtocolTypeEnum::getValue)
                        .collect(Collectors.toList()))
                .append("schedulingMetadata", new Document(entity.getSchedulingMetadata()));
    }

    /**
     * Converts a MongoDB document to a domain entity.
     *
     * @param doc The document to convert
     * @return The domain entity
     */
    private DomainEntityScanTask convertToEntity(Document doc) {
        List<String> protocolStrings = doc.getList("protocolTypes", String.class);
        List<SharedProtocolTypeEnum> protocols = protocolStrings.stream()
                .map(SharedProtocolTypeEnum::valueOf)
                .collect(Collectors.toList());

        Document metadataDoc = doc.get("schedulingMetadata", Document.class);
        Map<String, Object> metadata = metadataDoc.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return DomainEntityScanTask.builder()
                .id(doc.getString("id"))
                .targetUrls(doc.getList("targetUrls", String.class))
                .credentials(doc.getString("credentials"))
                .scanningDepth(doc.getInteger("scanningDepth"))
                .protocolTypes(protocols)
                .schedulingMetadata(metadata)
                .build();
    }
}

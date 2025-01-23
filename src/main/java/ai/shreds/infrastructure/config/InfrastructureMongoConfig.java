package ai.shreds.infrastructure.config;

import ai.shreds.infrastructure.exceptions.InfrastructureExceptionMongo;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * MongoDB configuration class.
 * Provides MongoDB client configuration and connection management.
 */
@Slf4j
@Configuration
@Getter
@Setter
public class InfrastructureMongoConfig {

    @Value("${mongodb.uri:}")
    private String mongoUri;

    @Value("${mongodb.database}")
    private String databaseName;

    @Value("${mongodb.username:}")
    private String username;

    @Value("${mongodb.password:}")
    private String password;

    @Value("${mongodb.host:localhost}")
    private String host;

    @Value("${mongodb.port:27017}")
    private int port;

    @Value("${mongodb.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${mongodb.max-connection-idle-time:300000}")
    private int maxConnectionIdleTime;

    @Value("${mongodb.max-connection-life-time:600000}")
    private int maxConnectionLifeTime;

    @Value("${mongodb.max-pool-size:100}")
    private int maxPoolSize;

    @Value("${mongodb.min-pool-size:5}")
    private int minPoolSize;

    private MongoClient mongoClient;

    /**
     * Creates and configures the MongoDB client.
     *
     * @return Configured MongoClient instance
     */
    @Bean
    public MongoClient getMongoClient() {
        if (mongoClient != null) {
            return mongoClient;
        }

        try {
            // Create codec registry for POJO support
            CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            // Build connection string
            String connectionString = buildConnectionString();
            log.debug("Initializing MongoDB client with connection string: {}", 
                    connectionString.replace(password, "*****"));

            // Configure client settings
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .codecRegistry(pojoCodecRegistry)
                    .applyToConnectionPoolSettings(builder -> builder
                            .maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.MILLISECONDS)
                            .maxConnectionLifeTime(maxConnectionLifeTime, TimeUnit.MILLISECONDS)
                            .maxSize(maxPoolSize)
                            .minSize(minPoolSize))
                    .applyToSocketSettings(builder -> builder
                            .connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS))
                    .build();

            mongoClient = MongoClients.create(settings);
            validateConnection();
            log.info("MongoDB client initialized successfully");

            return mongoClient;

        } catch (Exception e) {
            log.error("Failed to initialize MongoDB client", e);
            throw new InfrastructureExceptionMongo("Failed to initialize MongoDB client", e);
        }
    }

    /**
     * Builds the MongoDB connection string based on configuration.
     *
     * @return The connection string
     */
    private String buildConnectionString() {
        if (mongoUri != null && !mongoUri.trim().isEmpty()) {
            return mongoUri;
        }

        StringBuilder builder = new StringBuilder("mongodb://");

        // Add credentials if provided
        if (username != null && !username.trim().isEmpty() 
                && password != null && !password.trim().isEmpty()) {
            builder.append(username)
                   .append(":")
                   .append(password)
                   .append("@");
        }

        // Add host and port
        builder.append(host)
               .append(":")
               .append(port);

        // Add database if provided
        if (databaseName != null && !databaseName.trim().isEmpty()) {
            builder.append("/")
                   .append(databaseName);
        }

        return builder.toString();
    }

    /**
     * Validates the MongoDB connection by attempting to list databases.
     *
     * @throws InfrastructureExceptionMongo if validation fails
     */
    private void validateConnection() {
        try {
            mongoClient.listDatabases().first();
        } catch (Exception e) {
            throw new InfrastructureExceptionMongo("Failed to validate MongoDB connection", e);
        }
    }

    /**
     * Closes the MongoDB client when the application shuts down.
     */
    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                log.info("MongoDB client closed successfully");
            } catch (Exception e) {
                log.error("Error closing MongoDB client", e);
            }
        }
    }
}

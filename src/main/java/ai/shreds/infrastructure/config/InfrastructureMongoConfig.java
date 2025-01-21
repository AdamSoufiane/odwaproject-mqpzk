package ai.shreds.infrastructure.config;

import ai.shreds.infrastructure.config.properties.MongoProperties;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SslSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class InfrastructureMongoConfig {

    private final MongoProperties mongoProperties;

    public InfrastructureMongoConfig(MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
    }

    @Bean
    public MongoClient mongoClient() {
        log.info("Initializing MongoDB client with host: {}, port: {}", 
                mongoProperties.getHost(), mongoProperties.getPort());

        MongoClientSettings settings = getConnectionSettings();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        log.info("Creating MongoTemplate for database: {}", mongoProperties.getDatabase());
        return new MongoTemplate(mongoClient, mongoProperties.getDatabase());
    }

    private MongoClientSettings getConnectionSettings() {
        MongoClientSettings.Builder builder = MongoClientSettings.builder()
            .applyToClusterSettings(this::configureClusterSettings)
            .applyToConnectionPoolSettings(this::configurePoolSettings)
            .applyToSocketSettings(this::configureSocketSettings)
            .applyToSslSettings(this::configureSslSettings);

        if (mongoProperties.getUsername() != null && !mongoProperties.getUsername().isEmpty()) {
            builder.credential(MongoCredential.createCredential(
                mongoProperties.getUsername(),
                mongoProperties.getDatabase(),
                mongoProperties.getPassword().toCharArray()
            ));
        }

        return builder.build();
    }

    private void configureClusterSettings(ClusterSettings.Builder settings) {
        settings.hosts(Collections.singletonList(
            new ServerAddress(mongoProperties.getHost(), mongoProperties.getPort())
        ));
    }

    private void configurePoolSettings(ConnectionPoolSettings.Builder settings) {
        settings
            .maxWaitTime(mongoProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
            .maxSize(100)
            .minSize(5);
    }

    private void configureSocketSettings(SocketSettings.Builder settings) {
        settings
            .connectTimeout(mongoProperties.getConnectionTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(mongoProperties.getSocketTimeout(), TimeUnit.MILLISECONDS);
    }

    private void configureSslSettings(SslSettings.Builder settings) {
        settings.enabled(mongoProperties.isSsl());
    }
}

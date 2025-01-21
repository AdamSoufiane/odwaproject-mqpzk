package ai.shreds.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mongodb")
public class MongoProperties {
    private String host = "localhost";
    private int port = 27017;
    private String database = "shredsdb";
    private String username;
    private String password;
    private boolean ssl = false;
    private int connectionTimeout = 5000;
    private int socketTimeout = 60000;
}

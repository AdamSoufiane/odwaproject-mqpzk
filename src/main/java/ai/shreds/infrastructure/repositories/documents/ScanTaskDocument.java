package ai.shreds.infrastructure.repositories.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "scan_tasks")
public class ScanTaskDocument {
    @Id
    private String id;
    private List<String> targetUrls;
    private String credentials;
    private int scanningDepth;
    private List<String> protocolTypes;
    private String startTime;
}

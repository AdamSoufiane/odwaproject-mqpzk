package ai.shreds.infrastructure.repositories.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "raw_scan_results")
public class RawScanResultDocument {
    @Id
    private String id;
    private String scanTaskId;
    private List<String> vulnerabilityFindingsList;
    private String timestamp;
    private List<String> scanExecutionLogs;
    private String severity;
}

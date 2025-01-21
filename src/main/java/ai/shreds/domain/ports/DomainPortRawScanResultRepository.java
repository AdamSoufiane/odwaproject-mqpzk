package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import java.util.List;

public interface DomainPortRawScanResultRepository {
    void saveRawScanResult(DomainEntityRawScanResult result);
    List<DomainEntityRawScanResult> findByScanTaskId(String scanTaskId);
}

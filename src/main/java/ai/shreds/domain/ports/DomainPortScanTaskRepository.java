package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityScanTask;

public interface DomainPortScanTaskRepository {
    void saveScanTask(DomainEntityScanTask task);
    DomainEntityScanTask findById(String scanTaskId);
}

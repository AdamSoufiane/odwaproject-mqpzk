package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.entities.DomainEntityScanTask;

public interface DomainPortScanner {
    DomainEntityRawScanResult performProtocolScan(DomainEntityScanTask task);
}

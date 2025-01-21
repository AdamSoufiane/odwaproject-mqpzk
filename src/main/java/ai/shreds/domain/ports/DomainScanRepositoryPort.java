package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityRawScanResult;

public interface DomainScanRepositoryPort {
    void saveScanResult(DomainEntityRawScanResult result);
}

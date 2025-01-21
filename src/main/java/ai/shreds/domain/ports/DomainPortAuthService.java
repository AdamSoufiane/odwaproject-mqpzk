package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityScanTask;

public interface DomainPortAuthService {
    boolean checkAuthorization(DomainEntityScanTask task);
}

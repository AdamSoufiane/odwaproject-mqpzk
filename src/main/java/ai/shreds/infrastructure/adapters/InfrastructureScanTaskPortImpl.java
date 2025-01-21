package ai.shreds.infrastructure.adapters;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.exceptions.DomainErrorCode;
import ai.shreds.domain.ports.DomainPortScanner;
import ai.shreds.domain.ports.DomainScanTaskPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InfrastructureScanTaskPortImpl implements DomainScanTaskPort {

    private final DomainPortScanner domainPortScanner;

    public InfrastructureScanTaskPortImpl(DomainPortScanner domainPortScanner) {
        this.domainPortScanner = domainPortScanner;
    }

    @Override
    public DomainEntityRawScanResult executeScan(DomainEntityScanTask task) {
        log.info("Executing scan for task ID: {}", task.getId());

        try {
            DomainEntityRawScanResult result = domainPortScanner.performProtocolScan(task);
            log.info("Successfully executed scan for task ID: {}", task.getId());
            return result;

        } catch (DomainException e) {
            log.error("Domain error during scan execution: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during scan execution: {}", e.getMessage(), e);
            throw new DomainException(
                "Failed to execute scan",
                DomainErrorCode.SCAN_EXECUTION_ERROR,
                e
            );
        }
    }
}

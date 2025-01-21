package ai.shreds.infrastructure.adapters;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.exceptions.DomainErrorCode;
import ai.shreds.domain.ports.DomainPortRawScanResultRepository;
import ai.shreds.domain.ports.DomainScanRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InfrastructureScanRepositoryPortImpl implements DomainScanRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureScanRepositoryPortImpl.class);

    private final DomainPortRawScanResultRepository domainPortRawScanResultRepository;

    public InfrastructureScanRepositoryPortImpl(DomainPortRawScanResultRepository domainPortRawScanResultRepository) {
        this.domainPortRawScanResultRepository = domainPortRawScanResultRepository;
    }

    @Override
    public void saveScanResult(DomainEntityRawScanResult result) {
        log.info("Saving scan result for task ID: {}", result.getScanTaskId());

        try {
            domainPortRawScanResultRepository.saveRawScanResult(result);
            log.info("Successfully saved scan result for task ID: {}", result.getScanTaskId());

        } catch (DomainException e) {
            log.error("Domain error saving scan result: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error saving scan result: {}", e.getMessage(), e);
            throw new DomainException(
                "Failed to save scan result",
                DomainErrorCode.RESULT_SAVE_ERROR,
                e
            );
        }
    }
}

package ai.shreds.infrastructure.adapters;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.exceptions.DomainErrorCode;
import ai.shreds.domain.ports.DomainPortRawScanResultRepository;
import ai.shreds.domain.ports.DomainScanRepositoryPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InfrastructureScanRepositoryPortImpl implements DomainScanRepositoryPort {

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

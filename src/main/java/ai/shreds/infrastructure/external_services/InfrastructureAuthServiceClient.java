package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.exceptions.DomainErrorCode;
import ai.shreds.domain.ports.DomainPortAuthService;
lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InfrastructureAuthServiceClient implements DomainPortAuthService {

    // In a real implementation, this would be configured via properties
    private static final String AUTH_SERVICE_URL = "http://auth-service/api/v1/authorize";

    @Override
    public boolean checkAuthorization(DomainEntityScanTask task) {
        log.debug("Checking authorization for scan task ID: {}", task.getId());

        try {
            // Simulate external service call with random authorization
            boolean isAuthorized = simulateAuthCheck(task);

            if (!isAuthorized) {
                log.warn("Authorization denied for scan task ID: {}", task.getId());
                return false;
            }

            log.info("Authorization granted for scan task ID: {}", task.getId());
            return true;

        } catch (Exception e) {
            handleAuthError(e);
            return false;
        }
    }

    private boolean simulateAuthCheck(DomainEntityScanTask task) {
        // Simulate authorization logic
        // In a real implementation, this would make an HTTP call to the auth service
        if (task.getCredentials() == null || task.getCredentials().trim().isEmpty()) {
            throw new DomainException("Invalid credentials", DomainErrorCode.INVALID_CREDENTIALS);
        }

        // Simulate 90% success rate
        return Math.random() < 0.9;
    }

    private void handleAuthError(Exception error) {
        log.error("Authorization service error: {}", error.getMessage(), error);

        if (error instanceof DomainException) {
            throw (DomainException) error;
        }

        throw new DomainException(
            "Failed to communicate with authorization service",
            DomainErrorCode.AUTH_SERVICE_ERROR,
            error
        );
    }
}

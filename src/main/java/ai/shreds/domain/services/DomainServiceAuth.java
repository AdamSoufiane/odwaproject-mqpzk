package ai.shreds.domain.services;

import ai.shreds.domain.exceptions.DomainExceptionScanValidation;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Service responsible for validating authentication and authorization.
 * Handles credential validation and access control for security scans.
 */
@Slf4j
public class DomainServiceAuth {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$");
    private static final Pattern BASIC_AUTH_PATTERN = Pattern.compile("^Basic [A-Za-z0-9+/=]+$");

    /**
     * Validates the provided authorization token or credentials.
     * Supports both JWT tokens and Basic Authentication.
     *
     * @param credentials The authorization token or credentials to validate
     * @return true if the credentials are valid
     * @throws DomainExceptionScanValidation if validation fails
     */
    public boolean validateAuthorization(String credentials) {
        if (credentials == null || credentials.trim().isEmpty()) {
            log.warn("Empty credentials provided");
            return false;
        }

        try {
            if (isJwtToken(credentials)) {
                return validateJwtToken(credentials);
            } else if (isBasicAuth(credentials)) {
                return validateBasicAuth(credentials);
            } else {
                log.warn("Invalid credential format provided");
                return false;
            }
        } catch (Exception e) {
            log.error("Error validating credentials", e);
            throw new DomainExceptionScanValidation("Failed to validate credentials: " + e.getMessage());
        }
    }

    /**
     * Checks if the provided string is a JWT token.
     *
     * @param token The string to check
     * @return true if the string matches JWT format
     */
    private boolean isJwtToken(String token) {
        return TOKEN_PATTERN.matcher(token).matches();
    }

    /**
     * Checks if the provided string is a Basic Authentication header.
     *
     * @param auth The string to check
     * @return true if the string matches Basic Auth format
     */
    private boolean isBasicAuth(String auth) {
        return BASIC_AUTH_PATTERN.matcher(auth).matches();
    }

    /**
     * Validates a JWT token.
     * In a real implementation, this would verify the token signature and claims.
     *
     * @param token The JWT token to validate
     * @return true if the token is valid
     */
    private boolean validateJwtToken(String token) {
        try {
            // Split the token into its parts
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                log.warn("Invalid JWT token structure");
                return false;
            }

            // Decode the payload
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            log.debug("Successfully decoded JWT payload");

            // In a real implementation, we would:
            // 1. Verify the token signature
            // 2. Check token expiration
            // 3. Validate issuer and audience
            // 4. Check permissions/scopes

            return true;
        } catch (Exception e) {
            log.error("Error validating JWT token", e);
            return false;
        }
    }

    /**
     * Validates Basic Authentication credentials.
     * In a real implementation, this would verify the username and password.
     *
     * @param auth The Basic Authentication header
     * @return true if the credentials are valid
     */
    private boolean validateBasicAuth(String auth) {
        try {
            // Remove "Basic " prefix
            String credentials = auth.substring(6);
            // Decode credentials
            String decoded = new String(Base64.getDecoder().decode(credentials));
            String[] parts = decoded.split(":");

            if (parts.length != 2) {
                log.warn("Invalid Basic Auth format");
                return false;
            }

            String username = parts[0];
            String password = parts[1];

            // In a real implementation, we would:
            // 1. Validate against a user database
            // 2. Check password hash
            // 3. Verify user permissions

            log.debug("Successfully validated Basic Auth credentials for user: {}", username);
            return true;
        } catch (Exception e) {
            log.error("Error validating Basic Auth credentials", e);
            return false;
        }
    }
}

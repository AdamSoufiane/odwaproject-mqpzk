package ai.shreds.domain.value_objects;

import ai.shreds.domain.exceptions.DomainException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object representing scan configuration parameters.
 */
@Getter
public class DomainValueScanConfig {
    private static final int MAX_SCAN_DEPTH = 10;
    private static final List<String> SUPPORTED_PROTOCOLS = List.of("HTTP", "HTTPS", "FTP");

    private final String configParam;
    private final int scanDepth;
    private final List<String> protocols;

    public DomainValueScanConfig() {
        this.configParam = "default";
        this.scanDepth = 1;
        this.protocols = new ArrayList<>();
    }

    public DomainValueScanConfig(String configParam, int scanDepth, List<String> protocols) {
        this.configParam = configParam;
        this.scanDepth = scanDepth;
        this.protocols = new ArrayList<>(protocols);
        validate();
    }

    public void validate() {
        if (scanDepth <= 0 || scanDepth > MAX_SCAN_DEPTH) {
            throw new DomainException(String.format("Scan depth must be between 1 and %d", MAX_SCAN_DEPTH));
        }

        if (protocols == null || protocols.isEmpty()) {
            throw new DomainException("At least one protocol must be specified");
        }

        for (String protocol : protocols) {
            if (!SUPPORTED_PROTOCOLS.contains(protocol.toUpperCase())) {
                throw new DomainException("Unsupported protocol: " + protocol);
            }
        }
    }

    public List<String> getProtocols() {
        return Collections.unmodifiableList(protocols);
    }

    public DomainValueScanConfig withScanDepth(int newScanDepth) {
        return new DomainValueScanConfig(this.configParam, newScanDepth, this.protocols);
    }

    public DomainValueScanConfig withProtocols(List<String> newProtocols) {
        return new DomainValueScanConfig(this.configParam, this.scanDepth, newProtocols);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainValueScanConfig)) return false;
        DomainValueScanConfig that = (DomainValueScanConfig) o;
        return scanDepth == that.scanDepth &&
               configParam.equals(that.configParam) &&
               protocols.equals(that.protocols);
    }

    @Override
    public int hashCode() {
        int result = configParam.hashCode();
        result = 31 * result + scanDepth;
        result = 31 * result + protocols.hashCode();
        return result;
    }
}

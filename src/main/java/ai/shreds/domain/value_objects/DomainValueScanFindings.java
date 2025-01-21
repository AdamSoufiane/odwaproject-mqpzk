package ai.shreds.domain.value_objects;

import ai.shreds.domain.exceptions.DomainException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value object representing security scan findings.
 */
@Getter
public class DomainValueScanFindings {
    private final List<String> findingDetails;
    private final DomainValueSeverityEnum severity;

    public DomainValueScanFindings() {
        this.findingDetails = new ArrayList<>();
        this.severity = DomainValueSeverityEnum.INFO;
    }

    public DomainValueScanFindings(List<String> findingDetails, DomainValueSeverityEnum severity) {
        this.findingDetails = new ArrayList<>(findingDetails);
        this.severity = severity;
        validate();
    }

    public void validate() {
        if (findingDetails == null) {
            throw new DomainException("Finding details cannot be null");
        }

        for (String finding : findingDetails) {
            if (finding == null || finding.trim().isEmpty()) {
                throw new DomainException("Finding detail cannot be null or empty");
            }
        }

        if (severity == null) {
            throw new DomainException("Severity cannot be null");
        }
    }

    public List<String> getFindingDetails() {
        return Collections.unmodifiableList(findingDetails);
    }

    public DomainValueScanFindings withNewFinding(String finding) {
        if (finding == null || finding.trim().isEmpty()) {
            throw new DomainException("Finding cannot be null or empty");
        }
        List<String> newFindings = new ArrayList<>(this.findingDetails);
        newFindings.add(finding);
        return new DomainValueScanFindings(newFindings, this.severity);
    }

    public DomainValueScanFindings withSeverity(DomainValueSeverityEnum newSeverity) {
        return new DomainValueScanFindings(this.findingDetails, newSeverity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainValueScanFindings)) return false;
        DomainValueScanFindings that = (DomainValueScanFindings) o;
        return findingDetails.equals(that.findingDetails) && severity == that.severity;
    }

    @Override
    public int hashCode() {
        int result = findingDetails.hashCode();
        result = 31 * result + severity.hashCode();
        return result;
    }
}

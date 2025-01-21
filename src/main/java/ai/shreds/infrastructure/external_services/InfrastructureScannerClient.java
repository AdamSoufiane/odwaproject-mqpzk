package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityRawScanResult;
import ai.shreds.domain.entities.DomainEntityScanTask;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.exceptions.DomainErrorCode;
import ai.shreds.domain.ports.DomainPortScanner;
import ai.shreds.domain.value_objects.DomainValueScanFindings;
import ai.shreds.domain.value_objects.DomainValueSeverityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InfrastructureScannerClient implements DomainPortScanner {

    private static final Logger log = LoggerFactory.getLogger(InfrastructureScannerClient.class);

    @Override
    public DomainEntityRawScanResult performProtocolScan(DomainEntityScanTask task) {
        log.info("Starting protocol scan for task ID: {}", task.getId());

        try {
            List<String> executionLogs = new ArrayList<>();
            List<String> vulnerabilities = new ArrayList<>();

            for (String protocol : task.getProtocolTypes()) {
                log.debug("Executing scan for protocol: {} on task ID: {}", protocol, task.getId());
                ScanResult protocolResult = executeProtocolSpecificScan(protocol, task);
                vulnerabilities.addAll(protocolResult.vulnerabilities);
                executionLogs.addAll(protocolResult.logs);
            }

            DomainEntityRawScanResult result = createScanResult(task, vulnerabilities, executionLogs);
            log.info("Completed protocol scan for task ID: {}. Found {} vulnerabilities",
                    task.getId(), vulnerabilities.size());

            return result;

        } catch (Exception e) {
            log.error("Error during protocol scan: {}", e.getMessage(), e);
            throw new DomainException("Scanner error occurred", DomainErrorCode.SCANNER_ERROR, e);
        }
    }

    private DomainEntityRawScanResult createScanResult(DomainEntityScanTask task, List<String> vulnerabilities, List<String> logs) {
        DomainEntityRawScanResult result = new DomainEntityRawScanResult();
        result.setId(UUID.randomUUID().toString());
        result.setScanTaskId(task.getId());
        result.setVulnerabilityFindingsList(vulnerabilities);
        result.setTimestamp(LocalDateTime.now().toString());
        result.setScanExecutionLogs(logs);

        DomainValueScanFindings findings;
        if (!vulnerabilities.isEmpty()) {
            findings = new DomainValueScanFindings(vulnerabilities, DomainValueSeverityEnum.HIGH);
        } else {
            findings = new DomainValueScanFindings(vulnerabilities, DomainValueSeverityEnum.LOW);
        }
        result.setFindings(findings);

        return result;
    }

    private ScanResult executeProtocolSpecificScan(String protocol, DomainEntityScanTask task) {
        List<String> vulnerabilities = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        logs.add("Simulating scan for protocol: " + protocol);
        // Here we can simulate vulnerabilities
        if (Math.random() > 0.7) {
            vulnerabilities.add("Potential vulnerability found in protocol: " + protocol);
        }
        return new ScanResult(vulnerabilities, logs);
    }

    private static class ScanResult {
        List<String> vulnerabilities;
        List<String> logs;

        public ScanResult(List<String> vulnerabilities, List<String> logs) {
            this.vulnerabilities = vulnerabilities;
            this.logs = logs;
        }
    }
}

package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.value_objects.DomainValueSchedulingMetadata;
import ai.shreds.domain.value_objects.DomainValueScanConfig;
import ai.shreds.shared.dtos.SharedScanTaskDTO;
import ai.shreds.shared.enums.SharedScanStatusEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainEntityScanTask {
    private String id;
    private List<String> targetUrls;
    private String credentials;
    private int scanningDepth;
    private List<String> protocolTypes;
    private DomainValueSchedulingMetadata schedulingMetadata;
    private DomainValueScanConfig scanConfig;

    public DomainEntityScanTask() {
        this.targetUrls = new ArrayList<>();
        this.protocolTypes = new ArrayList<>();
        this.schedulingMetadata = new DomainValueSchedulingMetadata();
        this.scanConfig = new DomainValueScanConfig();
    }

    public DomainEntityScanTask(String id, List<String> targetUrls, String credentials,
                               int scanningDepth, List<String> protocolTypes,
                               DomainValueSchedulingMetadata schedulingMetadata) {
        this.id = id;
        this.targetUrls = new ArrayList<>(targetUrls);
        this.credentials = credentials;
        this.scanningDepth = scanningDepth;
        this.protocolTypes = new ArrayList<>(protocolTypes);
        this.schedulingMetadata = schedulingMetadata;
        this.scanConfig = new DomainValueScanConfig()
            .withScanDepth(scanningDepth)
            .withProtocols(protocolTypes);
        validate();
    }

    // Explicit setters to avoid Lombok issues
    public void setId(String id) {
        this.id = id;
    }
    public void setTargetUrls(List<String> targetUrls) {
        this.targetUrls = targetUrls;
    }
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }
    public void setScanningDepth(int scanningDepth) {
        this.scanningDepth = scanningDepth;
    }
    public void setProtocolTypes(List<String> protocolTypes) {
        this.protocolTypes = protocolTypes;
    }
    public void setSchedulingMetadata(DomainValueSchedulingMetadata schedulingMetadata) {
        this.schedulingMetadata = schedulingMetadata;
    }
    public void setScanConfig(DomainValueScanConfig scanConfig) {
        this.scanConfig = scanConfig;
    }

    public String getId() {
        return id;
    }
    public List<String> getTargetUrls() {
        return Collections.unmodifiableList(targetUrls);
    }
    public String getCredentials() {
        return credentials;
    }
    public int getScanningDepth() {
        return scanningDepth;
    }
    public List<String> getProtocolTypes() {
        return Collections.unmodifiableList(protocolTypes);
    }
    public DomainValueSchedulingMetadata getSchedulingMetadata() {
        return schedulingMetadata;
    }
    public DomainValueScanConfig getScanConfig() {
        return scanConfig;
    }

    public void addTargetUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new DomainException("Target URL cannot be null or empty");
        }
        this.targetUrls.add(url);
    }

    public void addProtocolType(String protocolType) {
        if (protocolType == null || protocolType.trim().isEmpty()) {
            throw new DomainException("Protocol type cannot be null or empty");
        }
        this.protocolTypes.add(protocolType.toUpperCase());
    }

    public void validate() {
        if (id == null || id.trim().isEmpty()) {
            throw new DomainException("Scan task ID is required");
        }
        if (targetUrls == null || targetUrls.isEmpty()) {
            throw new DomainException("At least one target URL is required");
        }
        if (protocolTypes == null || protocolTypes.isEmpty()) {
            throw new DomainException("At least one protocol type is required");
        }
        if (scanningDepth <= 0) {
            throw new DomainException("Scanning depth must be greater than 0");
        }
        if (schedulingMetadata == null) {
            throw new DomainException("Scheduling metadata is required");
        }
        scanConfig.validate();
        schedulingMetadata.validate();
    }

    public SharedScanTaskDTO toSharedDTO() {
        return new SharedScanTaskDTO(
            SharedScanStatusEnum.PENDING,
            this.id,
            "Scan task created successfully"
        );
    }

    public void updateScanConfig() {
        this.scanConfig = this.scanConfig.withScanDepth(this.scanningDepth)
                                         .withProtocols(this.protocolTypes);
        this.scanConfig.validate();
    }
}

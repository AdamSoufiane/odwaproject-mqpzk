package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedProtocolTypeEnum;
import ai.shreds.shared.value_objects.SharedValueSchedulingMetadata;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class SharedScanTaskRequestDTO {

    @NotBlank(message = "Scan task ID cannot be blank")
    private String scanTaskId;

    @NotEmpty(message = "Target URLs list cannot be empty")
    private List<@NotBlank(message = "Target URL cannot be blank") String> targetUrls;

    @NotBlank(message = "Credentials cannot be blank")
    private String credentials;

    @Min(value = 1, message = "Scanning depth must be at least 1")
    private int scanningDepth;

    @NotEmpty(message = "Protocol types list cannot be empty")
    private List<@NotNull(message = "Protocol type cannot be null") SharedProtocolTypeEnum> protocolTypes;

    @NotNull(message = "Scheduling metadata cannot be null")
    private SharedValueSchedulingMetadata schedulingMetadata;

    public SharedScanTaskRequestDTO() {
    }

    public SharedScanTaskRequestDTO(String scanTaskId,
                                     List<String> targetUrls,
                                     String credentials,
                                     int scanningDepth,
                                     List<SharedProtocolTypeEnum> protocolTypes,
                                     SharedValueSchedulingMetadata schedulingMetadata) {
        this.scanTaskId = scanTaskId;
        this.targetUrls = targetUrls;
        this.credentials = credentials;
        this.scanningDepth = scanningDepth;
        this.protocolTypes = protocolTypes;
        this.schedulingMetadata = schedulingMetadata;
    }

    public String getScanTaskId() {
        return scanTaskId;
    }

    public void setScanTaskId(String scanTaskId) {
        this.scanTaskId = scanTaskId;
    }

    public List<String> getTargetUrls() {
        return targetUrls;
    }

    public void setTargetUrls(List<String> targetUrls) {
        this.targetUrls = targetUrls;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public int getScanningDepth() {
        return scanningDepth;
    }

    public void setScanningDepth(int scanningDepth) {
        this.scanningDepth = scanningDepth;
    }

    public List<SharedProtocolTypeEnum> getProtocolTypes() {
        return protocolTypes;
    }

    public void setProtocolTypes(List<SharedProtocolTypeEnum> protocolTypes) {
        this.protocolTypes = protocolTypes;
    }

    public SharedValueSchedulingMetadata getSchedulingMetadata() {
        return schedulingMetadata;
    }

    public void setSchedulingMetadata(SharedValueSchedulingMetadata schedulingMetadata) {
        this.schedulingMetadata = schedulingMetadata;
    }
}

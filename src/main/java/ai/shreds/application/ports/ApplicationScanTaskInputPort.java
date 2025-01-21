package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedScanTaskDTO;
import ai.shreds.shared.dtos.SharedScanTaskRequestDTO;

public interface ApplicationScanTaskInputPort {

    SharedScanTaskDTO processScanTask(SharedScanTaskRequestDTO request);

}
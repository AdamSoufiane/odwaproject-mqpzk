package ai.shreds.application.ports;

import ai.shreds.shared.SharedScanTaskDTO;
import ai.shreds.shared.SharedScanTaskRequestDTO;

public interface ApplicationScanTaskInputPort {

    SharedScanTaskDTO processScanTask(SharedScanTaskRequestDTO request);

}
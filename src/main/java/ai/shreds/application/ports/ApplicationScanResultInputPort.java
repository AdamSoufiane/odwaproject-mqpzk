package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedScanCompletionDTO;
import ai.shreds.shared.dtos.SharedScanResultDTO;

public interface ApplicationScanResultInputPort {

    SharedScanResultDTO storeScanResult(SharedScanCompletionDTO request);

}
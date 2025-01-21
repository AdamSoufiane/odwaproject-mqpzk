package ai.shreds.application.ports;

import ai.shreds.shared.SharedScanCompletionDTO;
import ai.shreds.shared.SharedScanResultDTO;

public interface ApplicationScanResultInputPort {

    SharedScanResultDTO storeScanResult(SharedScanCompletionDTO request);

}
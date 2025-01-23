package ai.shreds.adapter.primary;

import ai.shreds.adapter.exceptions.AdapterExceptionInvalidScanMessage;
import ai.shreds.application.ports.ApplicationScanTaskInputPort;
import ai.shreds.shared.dtos.SharedScanTaskMessageDTO;
import ai.shreds.shared.dtos.SharedScanTaskResponseDTO;
import ai.shreds.shared.enums.SharedScanTaskStatusEnum;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Consumer for scan task messages from RabbitMQ.
 * Handles incoming scan task requests and delegates them to the application layer.
 */
@Slf4j
@Component
@Validated
public class AdapterScanTaskConsumer {

    private final ApplicationScanTaskInputPort applicationScanTaskInputPort;

    @Autowired
    public AdapterScanTaskConsumer(ApplicationScanTaskInputPort applicationScanTaskInputPort) {
        this.applicationScanTaskInputPort = applicationScanTaskInputPort;
    }

    /**
     * Consumes scan task messages from RabbitMQ.
     * Validates the message and delegates processing to the application layer.
     *
     * @param msg The scan task message to process
     * @return Response indicating the result of processing
     * @throws AdapterExceptionInvalidScanMessage if the message is invalid
     */
    @RabbitListener(queues = "${rabbitmq.queue.scan-tasks}")
    public SharedScanTaskResponseDTO consumeScanTaskMessage(@Valid @Payload SharedScanTaskMessageDTO msg) {
        log.info("Received scan task message with ID: {}", msg.getScanTaskId());

        try {
            validateMessage(msg);
            log.debug("Scan task message validation passed for ID: {}", msg.getScanTaskId());

            SharedScanTaskResponseDTO response = applicationScanTaskInputPort.processScanTask(msg);
            log.info("Successfully processed scan task with ID: {}, status: {}", 
                    msg.getScanTaskId(), response.getStatus());
            return response;

        } catch (AdapterExceptionInvalidScanMessage e) {
            log.error("Invalid scan task message received: {}", e.getMessage());
            return SharedScanTaskResponseDTO.builder()
                    .status(SharedScanTaskStatusEnum.INVALID)
                    .scanTaskId(msg.getScanTaskId())
                    .message("Invalid scan task: " + e.getMessage())
                    .build();

        } catch (Exception e) {
            log.error("Error processing scan task message: {}", e.getMessage(), e);
            return SharedScanTaskResponseDTO.builder()
                    .status(SharedScanTaskStatusEnum.FAILED)
                    .scanTaskId(msg.getScanTaskId())
                    .message("Error processing scan task: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Validates the scan task message.
     * Checks for required fields and valid values.
     *
     * @param msg The message to validate
     * @throws AdapterExceptionInvalidScanMessage if validation fails
     */
    private void validateMessage(SharedScanTaskMessageDTO msg) {
        if (msg == null) {
            throw new AdapterExceptionInvalidScanMessage("Scan task message cannot be null");
        }
        if (msg.getScanTaskId() == null || msg.getScanTaskId().trim().isEmpty()) {
            throw new AdapterExceptionInvalidScanMessage("Scan task ID cannot be empty");
        }
        if (msg.getTargetUrls() == null || msg.getTargetUrls().isEmpty()) {
            throw new AdapterExceptionInvalidScanMessage("Target URLs cannot be empty");
        }
        if (msg.getProtocolTypes() == null || msg.getProtocolTypes().isEmpty()) {
            throw new AdapterExceptionInvalidScanMessage("Protocol types cannot be empty");
        }
        if (msg.getScanningDepth() <= 0) {
            throw new AdapterExceptionInvalidScanMessage("Scanning depth must be greater than 0");
        }
        if (msg.getSchedulingMetadata() == null) {
            throw new AdapterExceptionInvalidScanMessage("Scheduling metadata cannot be null");
        }
    }
}

package ai.shreds.adapter.primary;

import ai.shreds.adapter.exceptions.AdapterException;
import ai.shreds.shared.dtos.SharedScanTaskRequestDTO;
import ai.shreds.shared.dtos.SharedScanTaskDTO;
import ai.shreds.application.ports.ApplicationScanTaskInputPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

@Component
public class AdapterScanTaskConsumer {
    private static final Logger log = LoggerFactory.getLogger(AdapterScanTaskConsumer.class);

    private final ApplicationScanTaskInputPort applicationScanTaskInputPort;
    private final ObjectMapper objectMapper;

    public AdapterScanTaskConsumer(ApplicationScanTaskInputPort applicationScanTaskInputPort,
                                   ObjectMapper objectMapper) {
        this.applicationScanTaskInputPort = applicationScanTaskInputPort;
        this.objectMapper = objectMapper;
    }

    public SharedScanTaskDTO consumeScanTask(@Valid SharedScanTaskRequestDTO request) {
        log.info("Processing scan task request for ID: {}", request.getScanTaskId());
        try {
            return applicationScanTaskInputPort.processScanTask(request);
        } catch (Exception e) {
            log.error("Error processing scan task: {}", e.getMessage(), e);
            throw new AdapterException("Failed to process scan task", "SCAN_TASK_PROCESSING_ERROR", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.scan-tasks}")
    public void consumeTask(Message message) {
        log.debug("Received message from queue");

        if (!validateMessage(message)) {
            log.error("Invalid message received");
            throw new AdapterException("Invalid message format", "INVALID_MESSAGE_FORMAT");
        }

        try {
            SharedScanTaskRequestDTO request = objectMapper.readValue(message.getBody(), SharedScanTaskRequestDTO.class);
            log.info("Successfully parsed scan task request for ID: {}", request.getScanTaskId());

            SharedScanTaskDTO response = consumeScanTask(request);
            log.info("Successfully processed scan task. Status: {}", response.getStatus());

        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            throw new AdapterException("Error processing message", "MESSAGE_PROCESSING_ERROR", e);
        }
    }

    private boolean validateMessage(Message message) {
        if (message == null || message.getBody() == null || message.getBody().length == 0) {
            log.warn("Received null or empty message");
            return false;
        }

        if (!message.getMessageProperties().getContentType().contains("json")) {
            log.warn("Invalid content type: {}", message.getMessageProperties().getContentType());
            return false;
        }

        return true;
    }
}

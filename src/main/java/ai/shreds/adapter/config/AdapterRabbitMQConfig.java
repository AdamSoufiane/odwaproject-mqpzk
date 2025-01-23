package ai.shreds.adapter.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RabbitMQ settings and beans.
 */
@Configuration
public class AdapterRabbitMQConfig {

    @Value("${rabbitmq.queue.scan-tasks}")
    private String scanTasksQueue;

    @Value("${rabbitmq.queue.scan-results}")
    private String scanResultsQueue;

    @Value("${rabbitmq.queue.dead-letter}")
    private String deadLetterQueue;

    /**
     * Creates the scan tasks queue with dead letter exchange configuration.
     *
     * @return The configured Queue instance
     */
    @Bean
    public Queue scanTasksQueue() {
        return QueueBuilder.durable(scanTasksQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", deadLetterQueue)
                .build();
    }

    /**
     * Creates the scan results queue.
     *
     * @return The configured Queue instance
     */
    @Bean
    public Queue scanResultsQueue() {
        return new Queue(scanResultsQueue, true);
    }

    /**
     * Creates the dead letter queue for failed messages.
     *
     * @return The configured Queue instance
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(deadLetterQueue, true);
    }

    /**
     * Creates a JSON message converter for RabbitMQ messages.
     *
     * @return The configured MessageConverter instance
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures the RabbitTemplate with JSON message conversion.
     *
     * @param connectionFactory The RabbitMQ connection factory
     * @return The configured RabbitTemplate instance
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

package ai.shreds.infrastructure.config;

import ai.shreds.infrastructure.config.properties.RabbitMQProperties;
import ai.shreds.infrastructure.exceptions.InfrastructureException;
import ai.shreds.infrastructure.exceptions.InfrastructureErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

@Slf4j
@Configuration
public class InfrastructureRabbitMQConfig {

    private final RabbitMQProperties rabbitProperties;

    public InfrastructureRabbitMQConfig(RabbitMQProperties rabbitProperties) {
        this.rabbitProperties = rabbitProperties;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        log.info("Initializing RabbitMQ connection factory with host: {}, port: {}",
                rabbitProperties.getHost(), rabbitProperties.getPort());

        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(rabbitProperties.getHost());
        factory.setPort(rabbitProperties.getPort());
        factory.setUsername(rabbitProperties.getUsername());
        factory.setPassword(rabbitProperties.getPassword());
        factory.setVirtualHost(rabbitProperties.getVirtualHost());
        factory.setConnectionTimeout(rabbitProperties.getConnectionTimeout());

        if (rabbitProperties.isSslEnabled()) {
            try {
                factory.getRabbitConnectionFactory().useSslProtocol();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                log.error("Failed to enable SSL protocol for RabbitMQ connection", e);
                throw new InfrastructureException("Failed to enable SSL for RabbitMQ", 
                    InfrastructureErrorCode.RABBITMQ_CONNECTION_ERROR);
            }
        }

        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        log.debug("Creating RabbitTemplate with message converter");
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public Queue scanTaskQueue() {
        log.info("Creating RabbitMQ queue: {}", rabbitProperties.getScanTaskQueue());
        return new Queue(rabbitProperties.getScanTaskQueue(), true);
    }
}
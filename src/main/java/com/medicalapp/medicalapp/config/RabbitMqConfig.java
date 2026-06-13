package com.medicalapp.medicalapp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RabbitMqConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqConfig.class);

    @Bean
    Queue eventosClinicosQueue(@Value("${medicalapp.rabbitmq.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    DirectExchange eventosClinicosExchange(@Value("${medicalapp.rabbitmq.exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Binding eventosClinicosBinding(
            Queue eventosClinicosQueue,
            DirectExchange eventosClinicosExchange,
            @Value("${medicalapp.rabbitmq.routing-key}") String routingKey
    ) {
        return BindingBuilder
                .bind(eventosClinicosQueue)
                .to(eventosClinicosExchange)
                .with(routingKey);
    }

    @Bean
    ApplicationRunner rabbitMqStartupLogger(
            @Value("${medicalapp.rabbitmq.queue}") String queueName,
            @Value("${medicalapp.rabbitmq.exchange}") String exchangeName,
            @Value("${medicalapp.rabbitmq.routing-key}") String routingKey
    ) {
        return args -> LOGGER.info(
                "RabbitMQ listo para eventos clinicos. queue={}, exchange={}, routingKey={}",
                queueName,
                exchangeName,
                routingKey
        );
    }
}

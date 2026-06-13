package com.medicalapp.medicalapp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

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
}

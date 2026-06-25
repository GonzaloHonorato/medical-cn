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
    Queue alertasOracleQueue(@Value("${medicalapp.rabbitmq.alert-oracle-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    Queue alertasArchivosQueue(@Value("${medicalapp.rabbitmq.alert-file-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    Queue resumenesSignosQueue(@Value("${medicalapp.rabbitmq.summary-queue}") String queueName) {
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
    Binding alertasOracleBinding(
            Queue alertasOracleQueue,
            DirectExchange eventosClinicosExchange,
            @Value("${medicalapp.rabbitmq.alert-routing-key}") String routingKey
    ) {
        return BindingBuilder
                .bind(alertasOracleQueue)
                .to(eventosClinicosExchange)
                .with(routingKey);
    }

    @Bean
    Binding alertasArchivosBinding(
            Queue alertasArchivosQueue,
            DirectExchange eventosClinicosExchange,
            @Value("${medicalapp.rabbitmq.alert-routing-key}") String routingKey
    ) {
        return BindingBuilder
                .bind(alertasArchivosQueue)
                .to(eventosClinicosExchange)
                .with(routingKey);
    }

    @Bean
    Binding resumenesSignosBinding(
            Queue resumenesSignosQueue,
            DirectExchange eventosClinicosExchange,
            @Value("${medicalapp.rabbitmq.summary-routing-key}") String routingKey
    ) {
        return BindingBuilder
                .bind(resumenesSignosQueue)
                .to(eventosClinicosExchange)
                .with(routingKey);
    }

    @Bean
    ApplicationRunner rabbitMqStartupLogger(
            @Value("${medicalapp.rabbitmq.queue}") String queueName,
            @Value("${medicalapp.rabbitmq.exchange}") String exchangeName,
            @Value("${medicalapp.rabbitmq.routing-key}") String routingKey,
            @Value("${medicalapp.rabbitmq.alert-oracle-queue}") String alertOracleQueue,
            @Value("${medicalapp.rabbitmq.alert-file-queue}") String alertFileQueue,
            @Value("${medicalapp.rabbitmq.alert-routing-key}") String alertRoutingKey,
            @Value("${medicalapp.rabbitmq.summary-queue}") String summaryQueue,
            @Value("${medicalapp.rabbitmq.summary-routing-key}") String summaryRoutingKey
    ) {
        return args -> {
            LOGGER.info(
                    "RabbitMQ listo para eventos clinicos. queue={}, exchange={}, routingKey={}",
                    queueName,
                    exchangeName,
                    routingKey
            );
            LOGGER.info(
                    "RabbitMQ listo para alertas medicas. oracleQueue={}, fileQueue={}, routingKey={}, summaryQueue={}, summaryRoutingKey={}",
                    alertOracleQueue,
                    alertFileQueue,
                    alertRoutingKey,
                    summaryQueue,
                    summaryRoutingKey
            );
        };
    }
}

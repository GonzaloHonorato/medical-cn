package com.medicalapp.medicalapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ResumenSignosVitalesQueueConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResumenSignosVitalesQueueConsumer.class);

    @RabbitListener(queues = "${medicalapp.rabbitmq.summary-queue}")
    public void consumir(byte[] body) {
        String payload = new String(body, StandardCharsets.UTF_8);
        LOGGER.info("Consumidor de resumenes recibio mensaje desde RabbitMQ: {}", payload);
    }
}

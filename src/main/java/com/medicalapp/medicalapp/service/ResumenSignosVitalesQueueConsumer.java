package com.medicalapp.medicalapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Consumidor del topico "resumenes-signos".
 * Registra en logs los resumenes periodicos publicados por el productor de resumenes.
 */
@Service
public class ResumenSignosVitalesQueueConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResumenSignosVitalesQueueConsumer.class);

    @KafkaListener(
            topics = "${medicalapp.kafka.topic.resumenes}",
            groupId = "${medicalapp.kafka.group.resumenes:resumenes}"
    )
    public void consumir(String payload) {
        LOGGER.info("Consumidor de resumenes recibio mensaje desde Kafka: {}", payload);
    }
}

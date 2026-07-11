package com.medicalapp.medicalapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

/**
 * Configuracion del cluster Kafka.
 * Declara los topicos del sistema de monitoreo; KafkaAdmin los crea al
 * arrancar contra el cluster (3 brokers en produccion, 1 en local).
 *
 * Nota: Kafka solo admite [a-zA-Z0-9._-] en nombres de topico, por lo que el
 * "señales_vitales" del enunciado se implementa como "senales_vitales".
 */
@Configuration
public class KafkaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    NewTopic senalesVitalesTopic(
            @Value("${medicalapp.kafka.topic.senales-vitales}") String nombre,
            @Value("${medicalapp.kafka.partitions}") int particiones,
            @Value("${medicalapp.kafka.replication-factor}") short replicacion
    ) {
        return TopicBuilder.name(nombre).partitions(particiones).replicas(replicacion).build();
    }

    @Bean
    NewTopic alertasTopic(
            @Value("${medicalapp.kafka.topic.alertas}") String nombre,
            @Value("${medicalapp.kafka.partitions}") int particiones,
            @Value("${medicalapp.kafka.replication-factor}") short replicacion
    ) {
        return TopicBuilder.name(nombre).partitions(particiones).replicas(replicacion).build();
    }

    @Bean
    NewTopic resumenesTopic(
            @Value("${medicalapp.kafka.topic.resumenes}") String nombre,
            @Value("${medicalapp.kafka.partitions}") int particiones,
            @Value("${medicalapp.kafka.replication-factor}") short replicacion
    ) {
        return TopicBuilder.name(nombre).partitions(particiones).replicas(replicacion).build();
    }

    @Bean
    ApplicationRunner kafkaStartupLogger(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${medicalapp.kafka.topic.senales-vitales}") String senalesTopic,
            @Value("${medicalapp.kafka.topic.alertas}") String alertasTopic,
            @Value("${medicalapp.kafka.topic.resumenes}") String resumenesTopic,
            @Value("${medicalapp.kafka.partitions}") int particiones,
            @Value("${medicalapp.kafka.replication-factor}") short replicacion
    ) {
        return args -> LOGGER.info(
                "Kafka listo. bootstrapServers={}, topicos=[{}, {}, {}], particiones={}, replicacion={}",
                bootstrapServers,
                senalesTopic,
                alertasTopic,
                resumenesTopic,
                particiones,
                replicacion
        );
    }
}

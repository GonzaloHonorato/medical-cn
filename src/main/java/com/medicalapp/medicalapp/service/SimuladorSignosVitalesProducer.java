package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.SenalVitalMessage;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.repository.PacienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Microservicio productor de señales vitales (Tarea 2).
 * Simula lecturas de dispositivos medicos para cada paciente activo a
 * intervalos regulares (cada segundo por defecto) y las publica en el
 * topico Kafka "senales_vitales". Un porcentaje de las lecturas se genera
 * fuera de rango para que el procesador dispare alertas.
 */
@Service
public class SimuladorSignosVitalesProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimuladorSignosVitalesProducer.class);

    private final PacienteRepository pacienteRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String senalesTopic;
    private final boolean enabled;

    public SimuladorSignosVitalesProducer(
            PacienteRepository pacienteRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${medicalapp.kafka.topic.senales-vitales}") String senalesTopic,
            @Value("${medicalapp.simulator.enabled}") boolean enabled
    ) {
        this.pacienteRepository = pacienteRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.senalesTopic = senalesTopic;
        this.enabled = enabled;
    }

    @Scheduled(fixedRateString = "${medicalapp.simulator.interval-ms}")
    public void generarSenales() {
        if (!enabled) {
            return;
        }

        List<Paciente> pacientes = pacienteRepository.findByActivoTrueOrderByHabitacionAsc();
        for (Paciente paciente : pacientes) {
            publicar(generarLectura(paciente));
        }
    }

    private SenalVitalMessage generarLectura(Paciente paciente) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        // ~20% de las lecturas se fuerza fuera de rango para provocar alertas.
        boolean anomala = random.nextInt(100) < 20;

        int frecuenciaCardiaca = anomala ? aleatorioEntre(random, 131, 165) : aleatorioEntre(random, 60, 100);
        int presionSistolica = anomala ? aleatorioEntre(random, 181, 210) : aleatorioEntre(random, 100, 130);
        int presionDiastolica = aleatorioEntre(random, 60, 85);
        int saturacionOxigeno = anomala ? aleatorioEntre(random, 80, 89) : aleatorioEntre(random, 94, 100);
        BigDecimal temperatura = BigDecimal.valueOf(anomala ? aleatorioDecimal(random, 38.6, 40.0) : aleatorioDecimal(random, 36.0, 37.4))
                .setScale(1, RoundingMode.HALF_UP);
        int frecuenciaRespiratoria = anomala ? aleatorioEntre(random, 31, 40) : aleatorioEntre(random, 12, 20);

        return new SenalVitalMessage(
                paciente.getId(),
                frecuenciaCardiaca,
                presionSistolica,
                presionDiastolica,
                saturacionOxigeno,
                temperatura,
                frecuenciaRespiratoria,
                OffsetDateTime.now()
        );
    }

    private void publicar(SenalVitalMessage lectura) {
        try {
            String key = String.valueOf(lectura.pacienteId());
            String payload = objectMapper.writeValueAsString(lectura);
            kafkaTemplate.send(senalesTopic, key, payload);
            LOGGER.debug("Productor publico señal vital en '{}' para paciente {}", senalesTopic, lectura.pacienteId());
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("No se pudo convertir la señal vital a JSON.", exception);
        }
    }

    private int aleatorioEntre(ThreadLocalRandom random, int min, int max) {
        return random.nextInt(min, max + 1);
    }

    private double aleatorioDecimal(ThreadLocalRandom random, double min, double max) {
        return random.nextDouble(min, max);
    }
}

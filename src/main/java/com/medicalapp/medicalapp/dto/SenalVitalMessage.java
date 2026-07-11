package com.medicalapp.medicalapp.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Mensaje que viaja por el topico Kafka "senales_vitales".
 * Representa una lectura de senales vitales generada por el productor
 * (simulador) o recibida desde un dispositivo/endpoint HTTP.
 */
public record SenalVitalMessage(
        Long pacienteId,
        Integer frecuenciaCardiaca,
        Integer presionSistolica,
        Integer presionDiastolica,
        Integer saturacionOxigeno,
        BigDecimal temperatura,
        Integer frecuenciaRespiratoria,
        OffsetDateTime fechaLectura
) {
}

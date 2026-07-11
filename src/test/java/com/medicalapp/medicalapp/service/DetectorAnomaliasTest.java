package com.medicalapp.medicalapp.service;

import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.model.EstadoPaciente;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SeveridadAlerta;
import com.medicalapp.medicalapp.model.SignoVital;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DetectorAnomaliasTest {

    private final DetectorAnomalias detector = new DetectorAnomalias();

    @Test
    void detectaAnomaliaCuandoLaSaturacionEsCritica() {
        Paciente paciente = paciente();
        SignoVital lectura = lectura(paciente, 80, 120, 80, 88, BigDecimal.valueOf(37.0), 18);

        List<EventoClinicoMessage> alertas = detector.detectar(paciente, lectura);

        assertThat(alertas)
                .extracting(EventoClinicoMessage::tipo)
                .contains("SATURACION_CRITICA");
        EventoClinicoMessage saturacion = alertas.stream()
                .filter(a -> a.tipo().equals("SATURACION_CRITICA"))
                .findFirst()
                .orElseThrow();
        assertThat(saturacion.pacienteId()).isEqualTo(1L);
        assertThat(saturacion.severidad()).isEqualTo(SeveridadAlerta.ALTA);
        assertThat(saturacion.valor()).isEqualTo("88%");
    }

    @Test
    void noDetectaAnomaliasCuandoLaLecturaEstaEnRangoNormal() {
        Paciente paciente = paciente();
        SignoVital lectura = lectura(paciente, 80, 120, 80, 97, BigDecimal.valueOf(37.0), 18);

        List<EventoClinicoMessage> alertas = detector.detectar(paciente, lectura);

        assertThat(alertas).isEmpty();
    }

    private Paciente paciente() {
        return new Paciente(
                1L,
                "Valentina Rojas",
                "18.245.991-2",
                56,
                "UCI-01",
                "Sepsis respiratoria",
                EstadoPaciente.CRITICO,
                true
        );
    }

    private SignoVital lectura(
            Paciente paciente,
            Integer frecuenciaCardiaca,
            Integer presionSistolica,
            Integer presionDiastolica,
            Integer saturacionOxigeno,
            BigDecimal temperatura,
            Integer frecuenciaRespiratoria
    ) {
        return new SignoVital(
                10L,
                paciente,
                frecuenciaCardiaca,
                presionSistolica,
                presionDiastolica,
                saturacionOxigeno,
                temperatura,
                frecuenciaRespiratoria,
                OffsetDateTime.parse("2026-06-25T12:00:00-04:00")
        );
    }
}

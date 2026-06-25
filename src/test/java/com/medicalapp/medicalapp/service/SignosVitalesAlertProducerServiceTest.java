package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.model.EstadoPaciente;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SeveridadAlerta;
import com.medicalapp.medicalapp.model.SignoVital;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.ArgumentCaptor;

class SignosVitalesAlertProducerServiceTest {

    private final RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final SignosVitalesAlertProducerService service = new SignosVitalesAlertProducerService(
            rabbitTemplate,
            objectMapper,
            "medicalapp.exchange",
            "alertas.clinicas"
    );

    @Test
    void publicaAlertaCuandoLaSaturacionEsCritica() throws Exception {
        Paciente paciente = paciente();
        SignoVital lectura = lectura(paciente, 80, 120, 80, 88, BigDecimal.valueOf(37.0), 18);

        service.publicarAlertas(paciente, lectura);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                eq("medicalapp.exchange"),
                eq("alertas.clinicas"),
                payloadCaptor.capture(),
                any(MessagePostProcessor.class)
        );
        EventoClinicoMessage message = objectMapper.readValue(payloadCaptor.getValue(), EventoClinicoMessage.class);
        assertThat(message.pacienteId()).isEqualTo(1L);
        assertThat(message.tipo()).isEqualTo("SATURACION_CRITICA");
        assertThat(message.severidad()).isEqualTo(SeveridadAlerta.ALTA);
        assertThat(message.valor()).isEqualTo("88%");
    }

    @Test
    void noPublicaAlertaCuandoLaLecturaEstaEnRangoNormal() {
        Paciente paciente = paciente();
        SignoVital lectura = lectura(paciente, 80, 120, 80, 97, BigDecimal.valueOf(37.0), 18);

        service.publicarAlertas(paciente, lectura);

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString(), any(MessagePostProcessor.class));
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

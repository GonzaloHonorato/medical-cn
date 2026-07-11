package com.medicalapp.medicalapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicalapp.medicalapp.dto.ResumenSignosVitalesMessage;
import com.medicalapp.medicalapp.model.EstadoPaciente;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SignoVital;
import com.medicalapp.medicalapp.repository.PacienteRepository;
import com.medicalapp.medicalapp.repository.SignoVitalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class ResumenSignosVitalesProducerServiceTest {

    private final PacienteRepository pacienteRepository = mock(PacienteRepository.class);
    private final SignoVitalRepository signoVitalRepository = mock(SignoVitalRepository.class);
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ResumenSignosVitalesProducerService service = new ResumenSignosVitalesProducerService(
            pacienteRepository,
            signoVitalRepository,
            kafkaTemplate,
            objectMapper,
            "resumenes-signos",
            true
    );

    @Test
    void publicaResumenConUltimosSignosVitalesDePacientesActivos() throws Exception {
        Paciente paciente = paciente();
        SignoVital lectura = lectura(paciente);
        when(pacienteRepository.findByActivoTrueOrderByHabitacionAsc()).thenReturn(List.of(paciente));
        when(signoVitalRepository.findTopByPacienteIdOrderByFechaRegistroDesc(1L)).thenReturn(Optional.of(lectura));

        service.publicarResumenProgramado();

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("resumenes-signos"), payloadCaptor.capture());
        ResumenSignosVitalesMessage message = objectMapper.readValue(
                payloadCaptor.getValue(),
                ResumenSignosVitalesMessage.class
        );
        assertThat(message.pacientesActivos()).isEqualTo(1);
        assertThat(message.lecturasIncluidas()).isEqualTo(1);
        assertThat(message.pacientes()).hasSize(1);
        assertThat(message.pacientes().get(0).ultimoSignoVital().saturacionOxigeno()).isEqualTo(96);
    }

    private Paciente paciente() {
        return new Paciente(
                1L,
                "Hector Munoz",
                "12.884.221-8",
                68,
                "UCI-02",
                "Postoperatorio cardiaco",
                EstadoPaciente.OBSERVACION,
                true
        );
    }

    private SignoVital lectura(Paciente paciente) {
        return new SignoVital(
                22L,
                paciente,
                88,
                124,
                78,
                96,
                BigDecimal.valueOf(37.2),
                18,
                OffsetDateTime.parse("2026-06-25T12:00:00-04:00")
        );
    }
}

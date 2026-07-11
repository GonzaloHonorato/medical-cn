package com.medicalapp.medicalapp.service;

import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.dto.EventoClinicoResponse;
import com.medicalapp.medicalapp.model.AlertaMedica;
import com.medicalapp.medicalapp.model.EventoClinico;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SeveridadAlerta;
import com.medicalapp.medicalapp.repository.AlertaMedicaRepository;
import com.medicalapp.medicalapp.repository.EventoClinicoRepository;
import com.medicalapp.medicalapp.repository.PacienteRepository;
import com.medicalapp.medicalapp.websocket.EventosClinicosWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class EventoClinicoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventoClinicoService.class);

    private final EventoClinicoRepository eventoClinicoRepository;
    private final PacienteRepository pacienteRepository;
    private final AlertaMedicaRepository alertaMedicaRepository;
    private final EventosClinicosWebSocketHandler webSocketHandler;

    public EventoClinicoService(
            EventoClinicoRepository eventoClinicoRepository,
            PacienteRepository pacienteRepository,
            AlertaMedicaRepository alertaMedicaRepository,
            EventosClinicosWebSocketHandler webSocketHandler
    ) {
        this.eventoClinicoRepository = eventoClinicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.alertaMedicaRepository = alertaMedicaRepository;
        this.webSocketHandler = webSocketHandler;
    }

    public List<EventoClinicoResponse> obtenerEventosRecientes() {
        return eventoClinicoRepository.findTop20ByOrderByFechaRecepcionDesc()
                .stream()
                .map(this::mapEvento)
                .toList();
    }

    @Transactional
    public EventoClinicoResponse registrarDesdeCola(EventoClinicoMessage message) {
        OffsetDateTime ahora = OffsetDateTime.now();
        Paciente paciente = buscarPaciente(message.pacienteId());
        SeveridadAlerta severidad = message.severidad() == null ? SeveridadAlerta.BAJA : message.severidad();

        EventoClinico evento = eventoClinicoRepository.save(new EventoClinico(
                null,
                paciente,
                textoOValor(message.tipo(), "EVENTO_CLINICO"),
                textoOValor(message.origen(), "Kafka"),
                textoOValor(message.mensaje(), "Evento clinico recibido desde el topico Kafka de alertas."),
                severidad,
                textoOpcional(message.valor()),
                message.fechaEvento() == null ? ahora : message.fechaEvento(),
                ahora
        ));

        if (paciente != null && (severidad == SeveridadAlerta.ALTA || severidad == SeveridadAlerta.MEDIA)) {
            alertaMedicaRepository.save(new AlertaMedica(
                    null,
                    paciente,
                    evento.getTipo(),
                    severidad,
                    evento.getMensaje(),
                    evento.getFechaRecepcion(),
                    false
            ));
        }

        EventoClinicoResponse response = mapEvento(evento);
        webSocketHandler.broadcast(response);
        LOGGER.info(
                "Evento clinico {} guardado desde Kafka para paciente {} con severidad {}",
                response.id(),
                response.pacienteNombre(),
                response.severidad()
        );
        return response;
    }

    private Paciente buscarPaciente(Long pacienteId) {
        if (pacienteId == null) {
            return null;
        }

        return pacienteRepository.findById(pacienteId)
                .filter(Paciente::getActivo)
                .orElse(null);
    }

    private EventoClinicoResponse mapEvento(EventoClinico evento) {
        Paciente paciente = evento.getPaciente();
        return new EventoClinicoResponse(
                evento.getId(),
                paciente == null ? null : paciente.getId(),
                paciente == null ? "Sin paciente asignado" : paciente.getNombre(),
                paciente == null ? "Externo" : paciente.getHabitacion(),
                evento.getTipo(),
                evento.getOrigen(),
                evento.getMensaje(),
                evento.getSeveridad(),
                evento.getValor(),
                evento.getFechaEvento(),
                evento.getFechaRecepcion()
        );
    }

    private String textoOValor(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value.trim();
    }

    private String textoOpcional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

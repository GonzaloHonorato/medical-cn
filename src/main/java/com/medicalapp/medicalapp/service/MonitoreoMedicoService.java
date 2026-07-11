package com.medicalapp.medicalapp.service;

import com.medicalapp.medicalapp.dto.AlertaResponse;
import com.medicalapp.medicalapp.dto.DashboardResponse;
import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.dto.ResumenPacienteResponse;
import com.medicalapp.medicalapp.dto.SignoVitalRequest;
import com.medicalapp.medicalapp.dto.SignoVitalResponse;
import com.medicalapp.medicalapp.model.AlertaMedica;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SignoVital;
import com.medicalapp.medicalapp.repository.AlertaMedicaRepository;
import com.medicalapp.medicalapp.repository.PacienteRepository;
import com.medicalapp.medicalapp.repository.SignoVitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class MonitoreoMedicoService {

    private final PacienteRepository pacienteRepository;
    private final SignoVitalRepository signoVitalRepository;
    private final AlertaMedicaRepository alertaMedicaRepository;
    private final EventoClinicoService eventoClinicoService;
    private final DetectorAnomalias detectorAnomalias;
    private final AlertaKafkaProducer alertaKafkaProducer;

    public MonitoreoMedicoService(
            PacienteRepository pacienteRepository,
            SignoVitalRepository signoVitalRepository,
            AlertaMedicaRepository alertaMedicaRepository,
            EventoClinicoService eventoClinicoService,
            DetectorAnomalias detectorAnomalias,
            AlertaKafkaProducer alertaKafkaProducer
    ) {
        this.pacienteRepository = pacienteRepository;
        this.signoVitalRepository = signoVitalRepository;
        this.alertaMedicaRepository = alertaMedicaRepository;
        this.eventoClinicoService = eventoClinicoService;
        this.detectorAnomalias = detectorAnomalias;
        this.alertaKafkaProducer = alertaKafkaProducer;
    }

    public DashboardResponse obtenerDashboard() {
        List<Paciente> pacientes = pacienteRepository.findByActivoTrueOrderByHabitacionAsc();
        List<ResumenPacienteResponse> resumenes = pacientes.stream()
                .map(this::mapResumenPaciente)
                .toList();
        List<AlertaResponse> alertas = alertaMedicaRepository.findTop20ByAtendidaFalseOrderByFechaRegistroDesc()
                .stream()
                .map(this::mapAlerta)
                .toList();

        return new DashboardResponse(
                pacientes.size(),
                alertaMedicaRepository.countByAtendidaFalse(),
                resumenes,
                alertas,
                eventoClinicoService.obtenerEventosRecientes()
        );
    }

    public List<AlertaResponse> obtenerAlertasActivas() {
        return alertaMedicaRepository.findTop20ByAtendidaFalseOrderByFechaRegistroDesc()
                .stream()
                .map(this::mapAlerta)
                .toList();
    }

    @Transactional
    public SignoVitalResponse registrarSignosVitales(SignoVitalRequest request) {
        validarLectura(request);
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .filter(Paciente::getActivo)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado o inactivo."));

        SignoVital lectura = new SignoVital(
                null,
                paciente,
                request.frecuenciaCardiaca(),
                request.presionSistolica(),
                request.presionDiastolica(),
                request.saturacionOxigeno(),
                request.temperatura(),
                request.frecuenciaRespiratoria(),
                OffsetDateTime.now()
        );

        SignoVital guardada = signoVitalRepository.save(lectura);
        for (EventoClinicoMessage alerta : detectorAnomalias.detectar(paciente, guardada)) {
            alertaKafkaProducer.enviar(alerta);
        }
        return mapSignoVital(guardada);
    }

    @Transactional
    public void atenderAlerta(Long id) {
        AlertaMedica alerta = alertaMedicaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alerta no encontrada."));
        alerta.setAtendida(true);
        alertaMedicaRepository.save(alerta);
    }

    private ResumenPacienteResponse mapResumenPaciente(Paciente paciente) {
        SignoVitalResponse ultimoSignoVital = signoVitalRepository
                .findTopByPacienteIdOrderByFechaRegistroDesc(paciente.getId())
                .map(this::mapSignoVital)
                .orElse(null);

        return new ResumenPacienteResponse(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getRut(),
                paciente.getEdad(),
                paciente.getHabitacion(),
                paciente.getDiagnostico(),
                paciente.getEstado(),
                ultimoSignoVital
        );
    }

    private SignoVitalResponse mapSignoVital(SignoVital signoVital) {
        Paciente paciente = signoVital.getPaciente();
        return new SignoVitalResponse(
                signoVital.getId(),
                paciente.getId(),
                paciente.getNombre(),
                paciente.getHabitacion(),
                signoVital.getFrecuenciaCardiaca(),
                signoVital.getPresionSistolica(),
                signoVital.getPresionDiastolica(),
                signoVital.getSaturacionOxigeno(),
                signoVital.getTemperatura(),
                signoVital.getFrecuenciaRespiratoria(),
                signoVital.getFechaRegistro()
        );
    }

    private AlertaResponse mapAlerta(AlertaMedica alerta) {
        Paciente paciente = alerta.getPaciente();
        return new AlertaResponse(
                alerta.getId(),
                paciente.getId(),
                paciente.getNombre(),
                paciente.getHabitacion(),
                alerta.getTipo(),
                alerta.getSeveridad(),
                alerta.getMensaje(),
                alerta.getFechaRegistro()
        );
    }

    private void validarLectura(SignoVitalRequest request) {
        if (request.pacienteId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un paciente.");
        }
        validarRango(request.frecuenciaCardiaca(), 20, 240, "Frecuencia cardiaca");
        validarRango(request.presionSistolica(), 50, 260, "Presion sistolica");
        validarRango(request.presionDiastolica(), 30, 180, "Presion diastolica");
        validarRango(request.saturacionOxigeno(), 50, 100, "Saturacion de oxigeno");
        validarRango(request.frecuenciaRespiratoria(), 4, 60, "Frecuencia respiratoria");
        if (request.temperatura() == null
                || request.temperatura().compareTo(BigDecimal.valueOf(30)) < 0
                || request.temperatura().compareTo(BigDecimal.valueOf(45)) > 0) {
            throw new IllegalArgumentException("Temperatura fuera de rango.");
        }
    }

    private void validarRango(Integer valor, int minimo, int maximo, String campo) {
        if (valor == null || valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(campo + " fuera de rango.");
        }
    }
}

package com.medicalapp.medicalapp.service;

import com.medicalapp.medicalapp.dto.AlertaResponse;
import com.medicalapp.medicalapp.dto.DashboardResponse;
import com.medicalapp.medicalapp.dto.ResumenPacienteResponse;
import com.medicalapp.medicalapp.dto.SignoVitalRequest;
import com.medicalapp.medicalapp.dto.SignoVitalResponse;
import com.medicalapp.medicalapp.model.AlertaMedica;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SeveridadAlerta;
import com.medicalapp.medicalapp.model.SignoVital;
import com.medicalapp.medicalapp.repository.AlertaMedicaRepository;
import com.medicalapp.medicalapp.repository.PacienteRepository;
import com.medicalapp.medicalapp.repository.SignoVitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonitoreoMedicoService {

    private final PacienteRepository pacienteRepository;
    private final SignoVitalRepository signoVitalRepository;
    private final AlertaMedicaRepository alertaMedicaRepository;
    private final EventoClinicoService eventoClinicoService;

    public MonitoreoMedicoService(
            PacienteRepository pacienteRepository,
            SignoVitalRepository signoVitalRepository,
            AlertaMedicaRepository alertaMedicaRepository,
            EventoClinicoService eventoClinicoService
    ) {
        this.pacienteRepository = pacienteRepository;
        this.signoVitalRepository = signoVitalRepository;
        this.alertaMedicaRepository = alertaMedicaRepository;
        this.eventoClinicoService = eventoClinicoService;
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
        generarAlertas(paciente, guardada);
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

    private void generarAlertas(Paciente paciente, SignoVital lectura) {
        List<AlertaMedica> alertas = new ArrayList<>();
        OffsetDateTime ahora = OffsetDateTime.now();

        if (lectura.getSaturacionOxigeno() < 90) {
            alertas.add(crearAlerta(paciente, "Saturacion critica", SeveridadAlerta.ALTA,
                    "Saturacion bajo 90%. Requiere revision inmediata.", ahora));
        }
        if (lectura.getFrecuenciaCardiaca() < 45 || lectura.getFrecuenciaCardiaca() > 130) {
            alertas.add(crearAlerta(paciente, "Frecuencia cardiaca critica", SeveridadAlerta.ALTA,
                    "Frecuencia cardiaca fuera de rango critico.", ahora));
        }
        if (lectura.getPresionSistolica() < 90 || lectura.getPresionSistolica() > 180) {
            alertas.add(crearAlerta(paciente, "Presion arterial critica", SeveridadAlerta.ALTA,
                    "Presion sistolica fuera de rango critico.", ahora));
        }
        if (lectura.getTemperatura().compareTo(BigDecimal.valueOf(38.5)) >= 0) {
            alertas.add(crearAlerta(paciente, "Fiebre alta", SeveridadAlerta.MEDIA,
                    "Temperatura superior o igual a 38.5 C.", ahora));
        }
        if (lectura.getFrecuenciaRespiratoria() < 8 || lectura.getFrecuenciaRespiratoria() > 30) {
            alertas.add(crearAlerta(paciente, "Frecuencia respiratoria anormal", SeveridadAlerta.MEDIA,
                    "Frecuencia respiratoria fuera de rango esperado.", ahora));
        }

        alertaMedicaRepository.saveAll(alertas);
    }

    private AlertaMedica crearAlerta(
            Paciente paciente,
            String tipo,
            SeveridadAlerta severidad,
            String mensaje,
            OffsetDateTime fecha
    ) {
        return new AlertaMedica(null, paciente, tipo, severidad, mensaje, fecha, false);
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

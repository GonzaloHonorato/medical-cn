package com.medicalapp.medicalapp.service;

import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.model.SeveridadAlerta;
import com.medicalapp.medicalapp.model.SignoVital;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de deteccion de anomalias en señales vitales.
 * Se comparte entre el procesador Kafka (topico senales_vitales -> alertas)
 * y el registro directo por HTTP, para no duplicar los umbrales clinicos.
 */
@Component
public class DetectorAnomalias {

    public List<EventoClinicoMessage> detectar(Paciente paciente, SignoVital lectura) {
        List<EventoClinicoMessage> alertas = new ArrayList<>();
        if (lectura.getSaturacionOxigeno() < 90) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "SATURACION_CRITICA",
                    SeveridadAlerta.ALTA,
                    "Saturacion bajo 90%. Requiere revision inmediata.",
                    lectura.getSaturacionOxigeno() + "%"
            ));
        }
        if (lectura.getFrecuenciaCardiaca() < 45 || lectura.getFrecuenciaCardiaca() > 130) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "FRECUENCIA_CARDIACA_CRITICA",
                    SeveridadAlerta.ALTA,
                    "Frecuencia cardiaca fuera de rango critico.",
                    lectura.getFrecuenciaCardiaca() + " lpm"
            ));
        }
        if (lectura.getPresionSistolica() < 90 || lectura.getPresionSistolica() > 180) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "PRESION_ARTERIAL_CRITICA",
                    SeveridadAlerta.ALTA,
                    "Presion sistolica fuera de rango critico.",
                    lectura.getPresionSistolica() + "/" + lectura.getPresionDiastolica() + " mmHg"
            ));
        }
        if (lectura.getTemperatura().compareTo(BigDecimal.valueOf(38.5)) >= 0) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "FIEBRE_ALTA",
                    SeveridadAlerta.MEDIA,
                    "Temperatura superior o igual a 38.5 C.",
                    lectura.getTemperatura() + " C"
            ));
        }
        if (lectura.getFrecuenciaRespiratoria() < 8 || lectura.getFrecuenciaRespiratoria() > 30) {
            alertas.add(crearMensaje(
                    paciente,
                    lectura,
                    "FRECUENCIA_RESPIRATORIA_ANORMAL",
                    SeveridadAlerta.MEDIA,
                    "Frecuencia respiratoria fuera de rango esperado.",
                    lectura.getFrecuenciaRespiratoria() + " rpm"
            ));
        }
        return alertas;
    }

    private EventoClinicoMessage crearMensaje(
            Paciente paciente,
            SignoVital lectura,
            String tipo,
            SeveridadAlerta severidad,
            String mensaje,
            String valor
    ) {
        return new EventoClinicoMessage(
                paciente.getId(),
                tipo,
                "Dispositivo medico UCI - " + paciente.getHabitacion(),
                mensaje,
                severidad,
                valor,
                lectura.getFechaRegistro()
        );
    }
}

package com.medicalapp.medicalapp.controller;

import com.medicalapp.medicalapp.dto.EventoPublicadoResponse;
import com.medicalapp.medicalapp.dto.SignoVitalRequest;
import com.medicalapp.medicalapp.service.MonitoreoMedicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint publico para empujar lecturas de señales vitales al topico Kafka
 * "senales_vitales". La lectura recorre el pipeline completo:
 * senales_vitales -> procesador -> alertas.
 */
@RestController
@RequestMapping("/public/signos-vitales")
public class SignosVitalesPublicController {

    private final MonitoreoMedicoService monitoreoMedicoService;

    public SignosVitalesPublicController(MonitoreoMedicoService monitoreoMedicoService) {
        this.monitoreoMedicoService = monitoreoMedicoService;
    }

    @PostMapping
    public ResponseEntity<EventoPublicadoResponse> registrar(@RequestBody SignoVitalRequest request) {
        return ResponseEntity.accepted().body(monitoreoMedicoService.publicarSenalVital(request));
    }
}

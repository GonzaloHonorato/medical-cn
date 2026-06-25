package com.medicalapp.medicalapp.controller;

import com.medicalapp.medicalapp.dto.SignoVitalRequest;
import com.medicalapp.medicalapp.dto.SignoVitalResponse;
import com.medicalapp.medicalapp.service.MonitoreoMedicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/signos-vitales")
public class SignosVitalesPublicController {

    private final MonitoreoMedicoService monitoreoMedicoService;

    public SignosVitalesPublicController(MonitoreoMedicoService monitoreoMedicoService) {
        this.monitoreoMedicoService = monitoreoMedicoService;
    }

    @PostMapping
    public ResponseEntity<SignoVitalResponse> registrar(@RequestBody SignoVitalRequest request) {
        return ResponseEntity.accepted().body(monitoreoMedicoService.registrarSignosVitales(request));
    }
}

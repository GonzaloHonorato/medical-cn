package com.medicalapp.medicalapp.controller;

import com.medicalapp.medicalapp.dto.AlertaResponse;
import com.medicalapp.medicalapp.dto.DashboardResponse;
import com.medicalapp.medicalapp.dto.SignoVitalRequest;
import com.medicalapp.medicalapp.dto.SignoVitalResponse;
import com.medicalapp.medicalapp.service.MonitoreoMedicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MonitoreoController {

    private final MonitoreoMedicoService monitoreoMedicoService;

    public MonitoreoController(MonitoreoMedicoService monitoreoMedicoService) {
        this.monitoreoMedicoService = monitoreoMedicoService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> obtenerDashboard() {
        return ResponseEntity.ok(monitoreoMedicoService.obtenerDashboard());
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaResponse>> obtenerAlertas() {
        return ResponseEntity.ok(monitoreoMedicoService.obtenerAlertasActivas());
    }

    @PatchMapping("/alertas/{id}/atender")
    public ResponseEntity<Void> atenderAlerta(@PathVariable Long id) {
        monitoreoMedicoService.atenderAlerta(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/signos-vitales")
    public ResponseEntity<SignoVitalResponse> registrarSignosVitales(@RequestBody SignoVitalRequest request) {
        return ResponseEntity.ok(monitoreoMedicoService.registrarSignosVitales(request));
    }
}

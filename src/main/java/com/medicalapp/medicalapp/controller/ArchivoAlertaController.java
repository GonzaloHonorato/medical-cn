package com.medicalapp.medicalapp.controller;

import com.medicalapp.medicalapp.dto.ArchivoAlertaResponse;
import com.medicalapp.medicalapp.service.ArchivoAlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/archivos-alertas")
public class ArchivoAlertaController {

    private final ArchivoAlertaService archivoAlertaService;

    public ArchivoAlertaController(ArchivoAlertaService archivoAlertaService) {
        this.archivoAlertaService = archivoAlertaService;
    }

    @GetMapping
    public ResponseEntity<List<ArchivoAlertaResponse>> listar() {
        return ResponseEntity.ok(archivoAlertaService.listarArchivos());
    }
}

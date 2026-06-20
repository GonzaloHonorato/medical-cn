package com.medicalapp.medicalapp.controller;

import com.medicalapp.medicalapp.dto.EventoClinicoMessage;
import com.medicalapp.medicalapp.dto.EventoPublicadoResponse;
import com.medicalapp.medicalapp.service.EventoClinicoProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/eventos-clinicos")
public class EventoClinicoProducerController {

    private final EventoClinicoProducerService producerService;

    public EventoClinicoProducerController(EventoClinicoProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    public ResponseEntity<EventoPublicadoResponse> publicar(@RequestBody EventoClinicoMessage request) {
        return ResponseEntity.accepted().body(producerService.publicar(request));
    }
}

package com.medicalapp.medicalapp.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Reenvía los 404 de rutas Angular a index.html (SPA routing).
 * Las rutas /api/** son respondidas por los controladores del BFF antes de llegar aquí.
 */
@Controller
public class SpaController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        // Solo redirigir 404 al frontend; otros errores los maneja Spring
        if (status != null && Integer.parseInt(status.toString()) == 404) {
            return "forward:/index.html";
        }
        return "forward:/index.html";
    }
}

package com.medicalapp.medicalapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiManagerHeaderFilter extends OncePerRequestFilter {

    private static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

    private final boolean required;
    private final String subscriptionKey;

    public ApiManagerHeaderFilter(
            @Value("${medicalapp.api-manager.required}") boolean required,
            @Value("${medicalapp.api-manager.subscription-key}") String subscriptionKey
    ) {
        this.required = required;
        this.subscriptionKey = subscriptionKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !required || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String providedKey = request.getHeader(SUBSCRIPTION_KEY_HEADER);

        if (!subscriptionKey.equals(providedKey)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud rechazada por API Manager");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

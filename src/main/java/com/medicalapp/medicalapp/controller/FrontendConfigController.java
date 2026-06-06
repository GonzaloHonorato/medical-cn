package com.medicalapp.medicalapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class FrontendConfigController {

    private final String apiBaseUrl;
    private final String authScopes;

    public FrontendConfigController(
            @Value("${medicalapp.frontend.api-base-url:}") String apiBaseUrl,
            @Value("${medicalapp.frontend.auth-scopes:openid profile}") String authScopes
    ) {
        this.apiBaseUrl = removeTrailingSlash(apiBaseUrl);
        this.authScopes = authScopes;
    }

    @GetMapping("/frontend-config.json")
    public FrontendConfigResponse getFrontendConfig() {
        return new FrontendConfigResponse(apiBaseUrl, parseScopes(authScopes));
    }

    private List<String> parseScopes(String value) {
        return Arrays.stream(value.split("[,\\s]+"))
                .map(String::trim)
                .filter(scope -> !scope.isBlank())
                .distinct()
                .toList();
    }

    private String removeTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    public record FrontendConfigResponse(String apiBaseUrl, List<String> authScopes) {
    }
}

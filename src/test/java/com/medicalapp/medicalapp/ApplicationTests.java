package com.medicalapp.medicalapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "medicalapp.simulator.enabled=false",
        "medicalapp.seed.enabled=false"
})
@ActiveProfiles("local")
class ApplicationTests {

    @Test
    void contextLoads() {
    }
}

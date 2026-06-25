package com.medicalapp.medicalapp.config;

import com.medicalapp.medicalapp.dto.SignoVitalRequest;
import com.medicalapp.medicalapp.model.EstadoPaciente;
import com.medicalapp.medicalapp.model.Paciente;
import com.medicalapp.medicalapp.repository.PacienteRepository;
import com.medicalapp.medicalapp.service.MonitoreoMedicoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    @ConditionalOnProperty(prefix = "medicalapp.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedPacientes(
            PacienteRepository pacienteRepository,
            MonitoreoMedicoService monitoreoMedicoService
    ) {
        return args -> {
            if (pacienteRepository.count() > 0) {
                return;
            }

            List<Paciente> pacientes = pacienteRepository.saveAll(List.of(
                    new Paciente(null, "Valentina Rojas", "18.245.991-2", 56, "UCI-01", "Sepsis respiratoria", EstadoPaciente.CRITICO, true),
                    new Paciente(null, "Hector Munoz", "12.884.221-8", 68, "UCI-02", "Postoperatorio cardiaco", EstadoPaciente.OBSERVACION, true),
                    new Paciente(null, "Camila Araya", "20.117.456-1", 34, "UCI-03", "Trauma toracico", EstadoPaciente.CRITICO, true),
                    new Paciente(null, "Marcelo Soto", "9.887.110-5", 73, "UCI-04", "Insuficiencia cardiaca", EstadoPaciente.ESTABLE, true)
            ));

            monitoreoMedicoService.registrarSignosVitales(new SignoVitalRequest(
                    pacientes.get(0).getId(), 136, 86, 52, 88, BigDecimal.valueOf(39.1), 32
            ));
            monitoreoMedicoService.registrarSignosVitales(new SignoVitalRequest(
                    pacientes.get(1).getId(), 88, 124, 78, 96, BigDecimal.valueOf(37.2), 18
            ));
            monitoreoMedicoService.registrarSignosVitales(new SignoVitalRequest(
                    pacientes.get(2).getId(), 118, 92, 60, 91, BigDecimal.valueOf(38.4), 24
            ));
            monitoreoMedicoService.registrarSignosVitales(new SignoVitalRequest(
                    pacientes.get(3).getId(), 76, 132, 82, 97, BigDecimal.valueOf(36.8), 16
            ));
        };
    }
}

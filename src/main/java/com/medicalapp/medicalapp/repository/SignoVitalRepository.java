package com.medicalapp.medicalapp.repository;

import com.medicalapp.medicalapp.model.SignoVital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignoVitalRepository extends JpaRepository<SignoVital, Long> {
    List<SignoVital> findTop20ByOrderByFechaRegistroDesc();

    Optional<SignoVital> findTopByPacienteIdOrderByFechaRegistroDesc(Long pacienteId);
}

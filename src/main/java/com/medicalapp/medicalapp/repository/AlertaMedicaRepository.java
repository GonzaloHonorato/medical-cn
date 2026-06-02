package com.medicalapp.medicalapp.repository;

import com.medicalapp.medicalapp.model.AlertaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaMedicaRepository extends JpaRepository<AlertaMedica, Long> {
    List<AlertaMedica> findTop20ByAtendidaFalseOrderByFechaRegistroDesc();

    long countByAtendidaFalse();
}

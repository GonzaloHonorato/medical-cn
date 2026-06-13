package com.medicalapp.medicalapp.repository;

import com.medicalapp.medicalapp.model.EventoClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoClinicoRepository extends JpaRepository<EventoClinico, Long> {
    List<EventoClinico> findTop20ByOrderByFechaRecepcionDesc();
}

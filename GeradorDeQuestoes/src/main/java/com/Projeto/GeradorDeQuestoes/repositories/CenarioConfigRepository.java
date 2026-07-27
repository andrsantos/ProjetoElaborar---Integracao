package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.entities.CenarioConfigEntity;

@Repository
public interface CenarioConfigRepository extends JpaRepository<CenarioConfigEntity, Long> {
    List<CenarioConfigEntity> findByTopicoAndNivel(String topico, String nivel);
    void deleteById(Long id);
} 
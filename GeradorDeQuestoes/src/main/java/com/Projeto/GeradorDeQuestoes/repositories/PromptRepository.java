package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Projeto.GeradorDeQuestoes.entities.PromptEntity;

public interface PromptRepository extends JpaRepository<PromptEntity, String> {

    @Query("SELECT p FROM PromptEntity p " +
           "WHERE p.disciplinaId = :disciplinaId OR p.isPadrao = true " +
           "ORDER BY p.isPadrao DESC, p.nome ASC")
    List<PromptEntity> buscarPromptsParaDisciplina(@Param("disciplinaId") String disciplinaId);



    
}

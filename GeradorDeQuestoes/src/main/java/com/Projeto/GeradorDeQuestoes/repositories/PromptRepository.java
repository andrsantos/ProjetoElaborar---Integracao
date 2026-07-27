package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Projeto.GeradorDeQuestoes.entities.PromptEntity;

public interface PromptRepository extends JpaRepository<PromptEntity, String> {

    List<PromptEntity> findByDocumento_Id(String documentoId);
    Optional<PromptEntity> findByDocumento_IdAndNivelAndAtivoTrue(String documentoId, String nivel);
    void deleteByDocumentoIdIn(List<String> documentosIds);
    
}

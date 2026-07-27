package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.entities.DocumentosReferenciaEntity;

@Repository
public interface DocumentosReferenciaRepository extends JpaRepository<DocumentosReferenciaEntity, String> {
    
    List<DocumentosReferenciaEntity> findByDisciplinaId(String disciplinaId);
    
    Optional<DocumentosReferenciaEntity> findByPdfBinarioId(UUID pdfBinarioId);
    
    List<DocumentosReferenciaEntity> findByTituloAndDisciplinaId(String titulo, String disciplinaId);

    void deleteByDisciplinaId(String disciplinaId);

}
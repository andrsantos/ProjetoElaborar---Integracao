package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.dto.PdfQuestaoResumoDTO;
import com.Projeto.GeradorDeQuestoes.entities.PdfQuestaoEntity;

@Repository
public interface PdfQuestaoRepository extends JpaRepository<PdfQuestaoEntity, UUID> {
    
    @Query("SELECT p.id AS id, p.nomeOriginal AS nomeOriginal, p.tamanhoBytes AS tamanhoBytes, p.dataUpload AS dataUpload, COUNT(q.id) AS quantidadeQuestoes " +
           "FROM PdfQuestaoEntity p LEFT JOIN p.questoesExtraidas q " +
           "GROUP BY p.id, p.nomeOriginal, p.tamanhoBytes, p.dataUpload " +
           "ORDER BY p.dataUpload DESC")
    List<PdfQuestaoResumoDTO> buscarTodosResumos();
    
}
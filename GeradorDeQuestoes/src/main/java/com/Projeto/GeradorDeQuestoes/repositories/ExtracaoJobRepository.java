package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.dto.JobResumoDTO;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;

@Repository
public interface ExtracaoJobRepository extends JpaRepository<ExtracaoJobEntity, String> {
    
    List<ExtracaoJobEntity> findAllByOrderByDataCriacaoDesc();
    
    @Query("SELECT new com.Projeto.GeradorDeQuestoes.dto.JobResumoDTO(" +
           "j.id, j.status, j.nomeArquivo, j.modoExtracao, j.mensagemErro, j.dataCriacao) " +
           "FROM ExtracaoJobEntity j " +
           "WHERE j.disciplinaId = :disciplinaId " +
           "ORDER BY j.dataCriacao DESC")
    List<JobResumoDTO> findResumoByDisciplinaIdOrderByDataCriacaoDesc(@Param("disciplinaId") String disciplinaId);
}
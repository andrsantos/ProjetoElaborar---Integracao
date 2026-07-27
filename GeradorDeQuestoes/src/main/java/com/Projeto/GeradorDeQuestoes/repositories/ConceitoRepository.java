package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.entities.ConceitoEntity;

@Repository
public interface ConceitoRepository extends JpaRepository<ConceitoEntity, UUID> {
    
    Optional<ConceitoEntity> findByNomeIgnoreCase(String nome);
    List<ConceitoEntity> findByDisciplina(String disciplina);
    void deleteByDisciplina(String idDisciplina);
    
}
package com.Projeto.GeradorDeQuestoes.repositories;

import com.Projeto.GeradorDeQuestoes.entities.DisciplinaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisciplinaRepository extends JpaRepository<DisciplinaEntity, String> {

    Optional<DisciplinaEntity> findByNomeIgnoreCase(String nome);
    List<DisciplinaEntity> findAllByUsuarioId(UUID usuarioId);
    
}
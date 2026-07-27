package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.entities.TopicoConfigEntity;

@Repository
public interface TopicoConfigRepository extends JpaRepository<TopicoConfigEntity, String> {
    Optional<TopicoConfigEntity> findByTopicoAndNivel(String topico, String nivel);
    void deleteById(String id);
}
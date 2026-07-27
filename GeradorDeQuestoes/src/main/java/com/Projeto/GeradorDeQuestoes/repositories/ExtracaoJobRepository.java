package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;

@Repository
public interface ExtracaoJobRepository extends JpaRepository<ExtracaoJobEntity, String> {
    List<ExtracaoJobEntity> findAllByOrderByDataCriacaoDesc();
    
}
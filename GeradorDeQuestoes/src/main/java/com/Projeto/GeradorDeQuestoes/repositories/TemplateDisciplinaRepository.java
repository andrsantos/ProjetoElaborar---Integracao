package com.Projeto.GeradorDeQuestoes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.Projeto.GeradorDeQuestoes.entities.TemplateDisciplinaEntity;
import java.util.UUID;

@Repository
public interface TemplateDisciplinaRepository extends JpaRepository<TemplateDisciplinaEntity, UUID> {
    
    boolean existsByNomeDisciplinaIgnoreCase(String nomeDisciplina);
    
}
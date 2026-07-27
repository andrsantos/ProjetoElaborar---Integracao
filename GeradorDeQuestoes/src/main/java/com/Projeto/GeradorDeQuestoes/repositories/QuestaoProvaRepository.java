package com.Projeto.GeradorDeQuestoes.repositories;

import com.Projeto.GeradorDeQuestoes.entities.QuestaoProvaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface QuestaoProvaRepository extends JpaRepository<QuestaoProvaEntity, UUID> {
    
}
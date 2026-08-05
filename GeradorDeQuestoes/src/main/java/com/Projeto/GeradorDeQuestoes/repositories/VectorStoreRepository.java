package com.Projeto.GeradorDeQuestoes.repositories;

import com.Projeto.GeradorDeQuestoes.entities.VectorStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface VectorStoreRepository extends JpaRepository<VectorStoreEntity, UUID> {

    @Modifying
    @Query(value = "DELETE FROM vector_store WHERE metadata->>'documento_id' = :documentoId", nativeQuery = true)
    void deleteByDocumentoId(@Param("documentoId") String documentoId);

    @Query(value = "SELECT DISTINCT metadata->>'conceito' AS conceito " +
                   "FROM vector_store " +
                   "WHERE metadata->>'documento_id' = :documentoId " +
                   "AND metadata->>'conceito' IS NOT NULL " +
                   "AND metadata->>'conceito' != ''", 
           nativeQuery = true)
    List<String> findDistinctConceitosByDocumentoId(@Param("documentoId") String documentoId);
    
    @Query(value = "SELECT DISTINCT metadata->>'conceito' AS conceito " +
                   "FROM vector_store " +
                   "WHERE metadata->>'disciplina_id' = :disciplinaId " +
                   "AND metadata->>'conceito' IS NOT NULL " +
                   "AND metadata->>'conceito' != ''", 
           nativeQuery = true)
    List<String> findDistinctConceitosByDisciplinaId(@Param("disciplinaId") String disciplinaId);

    @Query(value = "SELECT content FROM vector_store " +
                   "WHERE metadata->>'disciplina_id' = :disciplinaId", 
           nativeQuery = true)
    List<String> findAllChunksByDisciplinaId(@Param("disciplinaId") String disciplinaId);

    @Query(value = "SELECT content FROM vector_store " +
                   "WHERE metadata->>'disciplina_id' = :disciplinaId " +
                   "AND metadata->>'conceito' = :conceito", 
           nativeQuery = true)
    List<String> findChunksByDisciplinaAndConceito(@Param("disciplinaId") String disciplinaId, @Param("conceito") String conceito);


    
}
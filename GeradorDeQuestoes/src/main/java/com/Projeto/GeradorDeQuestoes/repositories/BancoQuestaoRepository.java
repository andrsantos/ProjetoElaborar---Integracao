package com.Projeto.GeradorDeQuestoes.repositories;

import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.PdfQuestaoEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BancoQuestaoRepository extends JpaRepository<BancoQuestaoEntity, UUID> {
        
 List<BancoQuestaoEntity> findByNivel(String nivel);
 List<BancoQuestaoEntity> findByConceito(String conceito);
List<BancoQuestaoEntity> findByDisciplinaId(String disciplinaId);

 @Query("SELECT DISTINCT q.arquivoOrigem FROM BancoQuestaoEntity q WHERE q.disciplinaId = :disciplinaId AND q.tipoDocumento = 'PROVA' AND q.arquivoOrigem IS NOT NULL")
 List<PdfQuestaoEntity> findProvasUnicasPorDisciplina(@Param("disciplinaId") String disciplinaId);

 @Query("SELECT DISTINCT q.conceito FROM BancoQuestaoEntity q WHERE q.disciplinaId = :disciplinaId AND q.conceito IS NOT NULL ORDER BY q.conceito ASC")
 List<String> findConceitosDistintosPorDisciplina(@Param("disciplinaId") String disciplinaId);

 void deleteByDisciplinaId(String disciplinaId);

 List<BancoQuestaoEntity> findByDisciplinaIdAndConceitoIn(String disciplinaId, List<String> conceitos); 



@Query(value = "SELECT * FROM tb_banco_questoes q " +
               "WHERE CAST(q.disciplina_id AS varchar) = CAST(:disciplinaId AS varchar) " +
               "AND q.conceito = :conceito " +
               "AND q.id NOT IN :idsExcluidos " +
               "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
Optional<BancoQuestaoEntity> buscarQuestaoAleatoriaParaSubstituicao(
        @Param("disciplinaId") String disciplinaId, 
        @Param("conceito") String conceito,
        @Param("idsExcluidos") List<UUID> idsExcluidos
);

long countByConceito(String conceito);

@Query("SELECT q FROM BancoQuestaoEntity q WHERE q.disciplinaId = :disciplinaId AND q.conceito = :conceito AND q.id NOT IN :idsExcluidos")
List<BancoQuestaoEntity> buscarCatalogoParaSubstituicao(
        @Param("disciplinaId") String disciplinaId,
        @Param("conceito") String conceito,
        @Param("idsExcluidos") List<UUID> idsExcluidos
);

List<BancoQuestaoEntity> findByArquivoOrigemId(UUID arquivoOrigemId);


}
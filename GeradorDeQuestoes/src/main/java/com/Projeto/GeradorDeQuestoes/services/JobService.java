package com.Projeto.GeradorDeQuestoes.services;

import java.util.List;

import com.Projeto.GeradorDeQuestoes.dto.JobResumoDTO;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;

public interface JobService {

    void salvar(ExtracaoJobEntity extracaoJob);
    ExtracaoJobEntity consultarStatusJob(String id);
    List<ExtracaoJobEntity> listarJobs();
    List<JobResumoDTO> listarJobsPorDisciplina( String disciplinaId);
    void deletarJob(String id);
    void consolidarJob(String id);
    void marcarVisualizadosPorDisciplina(String disciplinaId);
    void marcarJobComoVisualizado(String id);

}

package com.Projeto.GeradorDeQuestoes.services.impl;

import java.util.List;
import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.dto.JobResumoDTO;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.repositories.ExtracaoJobRepository;
import com.Projeto.GeradorDeQuestoes.services.JobService;

import jakarta.transaction.Transactional;

@Service
public class JobServiceImpl implements JobService {

    private final ExtracaoJobRepository jobRepository;

    public JobServiceImpl(ExtracaoJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    public ExtracaoJobEntity consultarStatusJob(String id) {
        return jobRepository.findById(id).orElse(null);
    }

    @Override
    public List<ExtracaoJobEntity> listarJobs() {
        return jobRepository.findAllByOrderByDataCriacaoDesc();
    }

    @Override
    public List<JobResumoDTO> listarJobsPorDisciplina(String disciplinaId) {
        return jobRepository.findResumoByDisciplinaIdOrderByDataCriacaoDesc(disciplinaId);
    }

    @Override
    public void deletarJob(String id) {
        ExtracaoJobEntity job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processamento não encontrado."));
        
        if ("PENDING".equals(job.getStatus()) || "PROCESSING".equals(job.getStatus())) {
            throw new IllegalStateException("Não é possível deletar um processamento que ainda está em andamento.");
        }
        
        jobRepository.delete(job);
    }

    @Override
    public void salvar(ExtracaoJobEntity extracaoJob) {
       jobRepository.save(extracaoJob);
    }


    @Override
    public void consolidarJob(String id) {
        ExtracaoJobEntity job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processamento não encontrado."));

        if (!"COMPLETED".equals(job.getStatus()) && !"PARCIALMENTE_CONCLUIDO".equals(job.getStatus())) {
            throw new IllegalStateException("Apenas processamentos concluídos podem ser consolidados no banco de dados.");
        }

        job.setStatus("CONSOLIDADO");
        jobRepository.save(job);
    }


    @Override
    @Transactional
    public void marcarVisualizadosPorDisciplina(String disciplinaId) {
        jobRepository.marcarJobsComoVisualizados(disciplinaId);
    }

    @Override
    public void marcarJobComoVisualizado(String id) {
        ExtracaoJobEntity job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processamento não encontrado."));
        
        job.setVisualizado(true);
        jobRepository.save(job);
    }


}
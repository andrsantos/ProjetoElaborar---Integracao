package com.Projeto.GeradorDeQuestoes.services.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.repositories.ExtracaoJobRepository;
import com.Projeto.GeradorDeQuestoes.services.ExtracaoJobService;

@Service
public class ExtracaoJobServiceImpl implements ExtracaoJobService {

    private final ExtracaoJobRepository jobRepository;


    public ExtracaoJobServiceImpl(ExtracaoJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }


    @Override
    public void salvar(ExtracaoJobEntity extracaoJob) {
       jobRepository.save(extracaoJob);
    }

    @Override
    public Optional<ExtracaoJobEntity> buscarPorId(String id) {
        return jobRepository.findById(id);
    }
    
}

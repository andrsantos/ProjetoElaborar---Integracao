package com.Projeto.GeradorDeQuestoes.services;

import java.util.Optional;

import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;

public interface ExtracaoJobService {

    void salvar(ExtracaoJobEntity extracaoJob);
    Optional<ExtracaoJobEntity> buscarPorId(String id);
    
}

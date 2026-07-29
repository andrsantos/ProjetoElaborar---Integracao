package com.Projeto.GeradorDeQuestoes.services;

import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

public interface CobrancaLlmService {
    
    void verificarSaldoMinimo(UsuarioEntity usuario);
    
    void deduzirCusto(UsuarioEntity usuario, long inputTokens, long outputTokens, String modelo);

}
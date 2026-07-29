package com.Projeto.GeradorDeQuestoes.services;

import java.util.List;

import com.Projeto.GeradorDeQuestoes.dto.GerarQuestaoRequest;
import com.Projeto.GeradorDeQuestoes.dto.ListaQuestoes;
import com.Projeto.GeradorDeQuestoes.dto.Questao;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

public interface GeradorQuestaoService {
    ListaQuestoes gerarQuestoes(GerarQuestaoRequest request, UsuarioEntity usuario);
    List<String> extrairConceitosUnicos(String contexto,int qtd, UsuarioEntity usuario);
    Questao gerarQuestaoSubstitutaAvulsa(String conceito, String enunciadoAntigo, String nivel, UsuarioEntity usuario);

}
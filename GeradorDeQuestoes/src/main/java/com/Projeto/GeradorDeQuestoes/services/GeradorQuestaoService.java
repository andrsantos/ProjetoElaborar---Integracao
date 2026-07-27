package com.Projeto.GeradorDeQuestoes.services;

import java.util.List;

import com.Projeto.GeradorDeQuestoes.dto.GerarQuestaoRequest;
import com.Projeto.GeradorDeQuestoes.dto.ListaQuestoes;
import com.Projeto.GeradorDeQuestoes.dto.Questao;

public interface GeradorQuestaoService {
    ListaQuestoes gerarQuestoes(GerarQuestaoRequest request);
    List<String> extrairConceitosUnicos(String contexto,int qtd);
    Questao gerarQuestaoSubstitutaAvulsa(String conceito, String enunciadoAntigo, String nivel);

}
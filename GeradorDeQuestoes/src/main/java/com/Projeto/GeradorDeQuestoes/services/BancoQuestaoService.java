package com.Projeto.GeradorDeQuestoes.services;

import java.util.List;

import com.Projeto.GeradorDeQuestoes.dto.ClassificacaoLoteDTO;
import com.Projeto.GeradorDeQuestoes.dto.GeracaoAutomaticaRequest;
import com.Projeto.GeradorDeQuestoes.dto.QuestaoDTO;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

public interface BancoQuestaoService {

    List<QuestaoDTO> listarQuestoes();
    List<QuestaoDTO> listarQuestoesPorNivel(String nivel);
    List<QuestaoDTO> gerarQuestoesParaProva(GeracaoAutomaticaRequest request);
    String normalizarConceito(String enunciado, String conceitoSugerido, List<String> conceitosExistentes, UsuarioEntity usuario);
    List<String> listarConceitosPorDisciplina(String disciplinaId);
    List<ClassificacaoLoteDTO> normalizarConceitosEmLote(String questoesJson, List<String> conceitosExistentes, UsuarioEntity usuario);
    void reorganizarBancoAssincrono(String disciplinaId, List<String> novaTaxonomia, UsuarioEntity usuario);

    
}

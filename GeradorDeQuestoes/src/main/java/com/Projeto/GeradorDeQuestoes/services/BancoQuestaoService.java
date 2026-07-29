package com.Projeto.GeradorDeQuestoes.services;

import java.util.List;
import com.Projeto.GeradorDeQuestoes.dto.GeracaoAutomaticaRequest;
import com.Projeto.GeradorDeQuestoes.dto.QuestaoDTO;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

public interface BancoQuestaoService {

    List<QuestaoDTO> listarQuestoes();
    List<QuestaoDTO> listarQuestoesPorTopico(String topico);
    List<QuestaoDTO> listarQuestoesPorNivel(String nivel);
    List<QuestaoDTO> gerarQuestoesParaProva(GeracaoAutomaticaRequest request);
    String normalizarConceito(String enunciado, String conceitoSugerido, List<String> conceitosExistentes, UsuarioEntity usuario);
    List<String> listarConceitosPorDisciplina(String disciplinaId);


    
}

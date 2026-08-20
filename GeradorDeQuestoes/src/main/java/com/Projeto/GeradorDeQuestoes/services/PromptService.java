package com.Projeto.GeradorDeQuestoes.services;

import java.util.List;

import com.Projeto.GeradorDeQuestoes.dto.PromptRequestDTO;
import com.Projeto.GeradorDeQuestoes.dto.PromptResponseDTO;

public interface PromptService {
    
    PromptResponseDTO criar(PromptRequestDTO dto);

    PromptResponseDTO buscarPorId(String id);
    
    PromptResponseDTO atualizar(String id, PromptRequestDTO dto);
    
    void alterarStatus(String id, boolean ativo);
    
    void deletar(String id);

    List<PromptResponseDTO> listarTodos();

    List<PromptResponseDTO> listarPorDisciplina(String disciplinaId);
    
}
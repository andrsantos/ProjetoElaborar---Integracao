package com.Projeto.GeradorDeQuestoes.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Projeto.GeradorDeQuestoes.dto.PromptRequestDTO;
import com.Projeto.GeradorDeQuestoes.dto.PromptResponseDTO;
import com.Projeto.GeradorDeQuestoes.entities.PromptEntity;
import com.Projeto.GeradorDeQuestoes.repositories.DocumentosReferenciaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.PromptRepository;
import com.Projeto.GeradorDeQuestoes.services.PromptService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PromptServiceImpl implements PromptService {

    private final PromptRepository promptRepository;

    public PromptServiceImpl(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    @Override
    @Transactional
    public PromptResponseDTO criar(PromptRequestDTO dto) {
        PromptEntity prompt = new PromptEntity();
        prompt.setNivel(dto.getNivel());
        prompt.setNome(dto.getNome());
        prompt.setInstrucao(dto.getInstrucao());
        prompt.setAtivo(dto.isAtivo());
        prompt.setDisciplinaId(dto.getDisciplinaId()); 
        prompt.setIsPadrao(false); 

        PromptEntity promptSalvo = promptRepository.save(prompt);

        return converterParaDTO(promptSalvo);
    }

    @Override
    public List<PromptResponseDTO> listarTodos() {
        List<PromptEntity> prompts = promptRepository.findAll();
        
        return prompts.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }



    @Override
    public PromptResponseDTO buscarPorId(String id) {
        PromptEntity prompt = promptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prompt não encontrado com o ID: " + id));
        
        return converterParaDTO(prompt);
    }


    @Override
    @Transactional
    public PromptResponseDTO atualizar(String id, PromptRequestDTO dto) {
        PromptEntity prompt = promptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prompt não encontrado com o ID: " + id));

        if (prompt.isIsPadrao()) {
            throw new IllegalStateException("Não é permitido editar um prompt padrão do sistema.");
        }

        prompt.setNivel(dto.getNivel());
        prompt.setInstrucao(dto.getInstrucao());
        prompt.setAtivo(dto.isAtivo());
        prompt.setNome(dto.getNome()); 

        PromptEntity promptAtualizado = promptRepository.save(prompt);
        return converterParaDTO(promptAtualizado);
    }

    @Override
    @Transactional
    public void alterarStatus(String id, boolean ativo) {
        PromptEntity prompt = promptRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prompt não encontrado com o ID: " + id));
        
        prompt.setAtivo(ativo);
        promptRepository.save(prompt);
    }

    @Override
    @Transactional
    public void deletar(String id) {
        if (!promptRepository.existsById(id)) {
            throw new EntityNotFoundException("Prompt não encontrado com o ID: " + id);
        }
        promptRepository.deleteById(id);
    }


    private PromptResponseDTO converterParaDTO(PromptEntity entity) {
        PromptResponseDTO dto = new PromptResponseDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setNivel(entity.getNivel());
        dto.setInstrucao(entity.getInstrucao());
        dto.setAtivo(entity.isAtivo());
        dto.setDisciplinaId(entity.getDisciplinaId());
        dto.setIsPadrao(entity.isIsPadrao());
        return dto;
    }


    @Override
    public List<PromptResponseDTO> listarPorDisciplina(String disciplinaId) {
        List<PromptEntity> prompts = promptRepository.buscarPromptsParaDisciplina(disciplinaId);
        
        return prompts.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }
}
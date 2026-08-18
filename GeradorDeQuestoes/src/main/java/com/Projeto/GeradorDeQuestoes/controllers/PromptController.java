package com.Projeto.GeradorDeQuestoes.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Projeto.GeradorDeQuestoes.dto.PromptRequestDTO;
import com.Projeto.GeradorDeQuestoes.dto.PromptResponseDTO;
import com.Projeto.GeradorDeQuestoes.services.PromptService;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {

    private final PromptService promptService;    

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping
    public ResponseEntity<PromptResponseDTO> criarPrompt(@Valid @RequestBody PromptRequestDTO dto) {
        System.out.println("Chegou " + dto.getNome());
        PromptResponseDTO novoPrompt = promptService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPrompt);
    }

    @GetMapping
    public ResponseEntity<List<PromptResponseDTO>> listarTodos() {
        List<PromptResponseDTO> prompts = promptService.listarTodos();
        return ResponseEntity.ok(prompts);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PromptResponseDTO> buscarPromptPorId(@PathVariable String id) {
        PromptResponseDTO prompt = promptService.buscarPorId(id);
        return ResponseEntity.ok(prompt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromptResponseDTO> atualizarPrompt(
            @PathVariable String id, 
            @Valid @RequestBody PromptRequestDTO dto) {
        
        PromptResponseDTO promptAtualizado = promptService.atualizar(id, dto);
        return ResponseEntity.ok(promptAtualizado);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatusPrompt(
            @PathVariable String id, 
            @RequestParam boolean ativo) {
        
        promptService.alterarStatus(id, ativo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPrompt(@PathVariable String id) {
        promptService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
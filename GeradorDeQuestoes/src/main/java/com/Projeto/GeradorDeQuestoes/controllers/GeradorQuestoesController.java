package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.dto.GerarQuestaoRequest;
import com.Projeto.GeradorDeQuestoes.dto.ListaQuestoes;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.services.GeradorQuestaoService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questoes")
@CrossOrigin(origins = "http://localhost:4200")
public class GeradorQuestoesController {

    private final GeradorQuestaoService geradorQuestaoService;

    public GeradorQuestoesController(GeradorQuestaoService geradorQuestaoService) {

        this.geradorQuestaoService = geradorQuestaoService;
        
    }

    @PostMapping("/gerar")
    public ListaQuestoes gerarQuestao(@RequestBody GerarQuestaoRequest request, @AuthenticationPrincipal UsuarioEntity usuario) {

        return this.geradorQuestaoService.gerarQuestoes(request, usuario);

    }
}
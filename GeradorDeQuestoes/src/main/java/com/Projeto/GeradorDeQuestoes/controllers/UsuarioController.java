package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.dto.UsuarioPerfilDTO;
import com.Projeto.GeradorDeQuestoes.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilDTO> obterMeuPerfil() {
        UsuarioPerfilDTO perfil = usuarioService.obterPerfilUsuarioLogado();
        return ResponseEntity.ok(perfil);
    }
}
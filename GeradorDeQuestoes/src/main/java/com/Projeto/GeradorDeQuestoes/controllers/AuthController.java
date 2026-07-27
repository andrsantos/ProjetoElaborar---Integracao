package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.dto.AuthenticationDTO;
import com.Projeto.GeradorDeQuestoes.dto.LoginResponseDTO;
import com.Projeto.GeradorDeQuestoes.dto.RegisterDTO;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.repositories.UsuarioRepository;
import com.Projeto.GeradorDeQuestoes.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getSenha());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var token = tokenService.gerarToken((UsuarioEntity) auth.getPrincipal());
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody RegisterDTO data) {
        if (this.repository.findByEmail(data.getEmail()) != null) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado no sistema.");
        }

        String encryptedPassword = passwordEncoder.encode(data.getSenha());
        UsuarioEntity novoUsuario = new UsuarioEntity(data.getEmail(), encryptedPassword);

        this.repository.save(novoUsuario);

        return ResponseEntity.ok().build();
    }
}
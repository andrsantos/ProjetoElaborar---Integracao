package com.Projeto.GeradorDeQuestoes.services.impl;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.dto.UsuarioPerfilDTO;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.repositories.UsuarioRepository;
import com.Projeto.GeradorDeQuestoes.services.UsuarioService;


@Service
public class UsuarioServiceImpl implements UsuarioService {

   private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }


    public UsuarioPerfilDTO obterPerfilUsuarioLogado() {
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UsuarioEntity usuario = (UsuarioEntity) usuarioRepository.findByEmail(email);

        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado.");
        }

        String nomeExibicao = (usuario.getNome() != null && !usuario.getNome().isBlank()) 
                              ? usuario.getNome() 
                              : "Professor(a)";

        return new UsuarioPerfilDTO(usuario.getId(), nomeExibicao, usuario.getEmail());
    }
    
}

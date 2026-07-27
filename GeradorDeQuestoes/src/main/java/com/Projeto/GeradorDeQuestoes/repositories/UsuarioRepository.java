package com.Projeto.GeradorDeQuestoes.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {
    
    UserDetails findByEmail(String email);
    Optional<UsuarioEntity> findOptionalByEmail(String email);
}
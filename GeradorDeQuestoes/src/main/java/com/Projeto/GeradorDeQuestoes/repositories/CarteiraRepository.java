package com.Projeto.GeradorDeQuestoes.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Projeto.GeradorDeQuestoes.entities.CarteiraEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

import java.util.Optional;

@Repository
public interface CarteiraRepository extends JpaRepository<CarteiraEntity, Long> {
    
    Optional<CarteiraEntity> findByUsuario(UsuarioEntity usuario);
    
}
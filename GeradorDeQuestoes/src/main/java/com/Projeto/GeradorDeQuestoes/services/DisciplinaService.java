package com.Projeto.GeradorDeQuestoes.services;

import java.util.List;
import java.util.Optional;
import com.Projeto.GeradorDeQuestoes.entities.DisciplinaEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

public interface DisciplinaService {

    List<DisciplinaEntity> listarTodas();
    List<DisciplinaEntity> listarTodasPorUsuario(UsuarioEntity usuarioLogado);
    Optional<DisciplinaEntity> buscarPorId(String id);
    DisciplinaEntity salvar(DisciplinaEntity disciplina, UsuarioEntity usuario);
    void deletar(String id);
    void deletarDisciplina(String id);
    
}

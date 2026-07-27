package com.Projeto.GeradorDeQuestoes.services;

import com.Projeto.GeradorDeQuestoes.entities.ConceitoEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConceitoService {
    
    ConceitoEntity salvar(ConceitoEntity conceito);
    
    Optional<ConceitoEntity> buscarPorId(UUID id);
    
    Optional<ConceitoEntity> buscarPorNome(String nome);
    
    List<ConceitoEntity> listarTodos();
    
    ConceitoEntity atualizar(UUID id, ConceitoEntity conceitoAtualizado);
    
    void deletar(UUID id);

    List<String> listarConceitosPorDisciplina(String disciplinaId);

    ConceitoEntity processarConceitoQuestao(String nomeConceito, String disciplina, ConceitoEntity.TipoOrigem novaOrigem, UUID origemId);

    List<String> gerarArvoreSemente(String nomeDisciplina, String descricaoDisciplina);

    ConceitoEntity salvarConceitoSemente(String nomeTopico, String disciplinaId);
    

}
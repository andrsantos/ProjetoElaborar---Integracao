package com.Projeto.GeradorDeQuestoes.services;

import java.util.List;
import java.util.UUID;

import com.Projeto.GeradorDeQuestoes.dto.PdfQuestaoResumoDTO;
import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.PdfQuestaoEntity;

public interface PdfQuestaoService {

    List<PdfQuestaoResumoDTO> buscarTodosResumos();
    PdfQuestaoEntity buscarPorId(UUID id);
    void deletarPorId(UUID id);
    List<BancoQuestaoEntity> buscarQuestoesPorProvaId(UUID provaId);
    
    
}

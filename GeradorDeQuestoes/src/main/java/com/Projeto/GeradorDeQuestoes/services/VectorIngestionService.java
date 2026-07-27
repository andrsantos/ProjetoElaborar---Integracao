package com.Projeto.GeradorDeQuestoes.services;

import java.util.Map;

import com.Projeto.GeradorDeQuestoes.dto.ResultadoIngestaoDTO;
import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;

public interface VectorIngestionService {

    ResultadoIngestaoDTO ingerirPdf(byte[] pdfBytes, String filename, Map<String, Object> metadata);
    void vetorizarQuestaoDeProva(BancoQuestaoEntity questao);
    
}

package com.Projeto.GeradorDeQuestoes.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.dto.PdfQuestaoResumoDTO;
import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.PdfQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.repositories.BancoQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.PdfQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.services.PdfQuestaoService;

@Service
public class PdfQuestaoServiceImpl implements PdfQuestaoService {

    @Autowired
    PdfQuestaoRepository pdfQuestaoRepository;

    @Autowired
    BancoQuestaoRepository bancoQuestaoRepository;



    @Override
    public List<PdfQuestaoResumoDTO> buscarTodosResumos() {

        return pdfQuestaoRepository.buscarTodosResumos();

    }


    @Override
    public PdfQuestaoEntity buscarPorId(UUID id) {
        return pdfQuestaoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prova não encontrada com o ID: " + id));
    }

    @Override
    public void deletarPorId(UUID id) {
        if (!pdfQuestaoRepository.existsById(id)) {
            throw new IllegalArgumentException("Não foi possível excluir. Prova não encontrada com o ID: " + id);
        }
        
        pdfQuestaoRepository.deleteById(id);
    }


    @Override
    public List<BancoQuestaoEntity> buscarQuestoesPorProvaId(UUID provaId) {
        if (!pdfQuestaoRepository.existsById(provaId)) {
            throw new IllegalArgumentException("Prova não encontrada com o ID: " + provaId);
        }
        
        return bancoQuestaoRepository.findByArquivoOrigemId(provaId);
    }


    @Override
    public List<PdfQuestaoResumoDTO> buscarResumosPorDisciplina(String disciplinaId) {
        return pdfQuestaoRepository.findResumosByDisciplinaId(disciplinaId);
    }


    
}

package com.Projeto.GeradorDeQuestoes.services.impl;

import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.dto.DocumentosReferenciaDTO;
import com.Projeto.GeradorDeQuestoes.entities.DisciplinaEntity;
import com.Projeto.GeradorDeQuestoes.entities.DocumentosReferenciaEntity;
import com.Projeto.GeradorDeQuestoes.entities.PdfBinarioEntity;
import com.Projeto.GeradorDeQuestoes.repositories.DisciplinaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.DocumentosReferenciaRepository;
import com.Projeto.GeradorDeQuestoes.services.DocumentosReferenciaService;

@Service
public class DocumentosReferenciaServiceImpl implements DocumentosReferenciaService {

    private DocumentosReferenciaRepository documentosReferenciaRepository;


    private DisciplinaRepository disciplinaRepository;


    DocumentosReferenciaServiceImpl(DocumentosReferenciaRepository documentosReferenciaRepository,
        DisciplinaRepository disciplinaRepository){
        this.documentosReferenciaRepository = documentosReferenciaRepository;
        this.disciplinaRepository = disciplinaRepository;

    }


    @Override
    public DocumentosReferenciaEntity SalvarDocumentoReferencia(DocumentosReferenciaDTO documentosReferenciaDTO) {

        DocumentosReferenciaEntity documentosReferenciaEntity = new DocumentosReferenciaEntity();

        
        return documentosReferenciaRepository.save(documentosReferenciaEntity);
    }




    public DocumentosReferenciaEntity vincularContexto(PdfBinarioEntity pdf, String titulo, String disciplinaId) {
 
        DisciplinaEntity disciplinaEntity = disciplinaRepository.findById(disciplinaId)
                .orElseThrow(() -> new IllegalArgumentException("Disciplina não encontrada com ID: " + disciplinaId));

        System.out.println("Cadastrando documento: " + titulo + " para a disciplina " + disciplinaEntity.getNome());
    
        DocumentosReferenciaEntity referencia = new DocumentosReferenciaEntity();
        referencia.setPdfBinario(pdf); 
        referencia.setDisciplina(disciplinaEntity); 
        referencia.setTitulo(titulo); 
    
        return documentosReferenciaRepository.save(referencia);
    }


    
    
}

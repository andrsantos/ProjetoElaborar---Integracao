package com.Projeto.GeradorDeQuestoes.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Projeto.GeradorDeQuestoes.dto.PdfQuestaoDTO;
import com.Projeto.GeradorDeQuestoes.repositories.PdfQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.VectorStoreRepository;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentoController {

    private final VectorStoreRepository vectorStoreRepository;
    private final PdfQuestaoRepository pdfQuestaoRepository;

    public DocumentoController( VectorStoreRepository vectorStoreRepository, 
        PdfQuestaoRepository pdfQuestaoRepository) {
        this.vectorStoreRepository = vectorStoreRepository;
        this.pdfQuestaoRepository = pdfQuestaoRepository;
    }

    @GetMapping("/{documentoId}/conceitos")
    public ResponseEntity<List<String>> listarConceitosPorDocumento(@PathVariable String documentoId) {
        List<String> conceitos = vectorStoreRepository.findDistinctConceitosByDocumentoId(documentoId);
        return ResponseEntity.ok(conceitos);
    }

    @GetMapping("/arquivo/{arquivoId}")
    public ResponseEntity<PdfQuestaoDTO> buscarArquivoPorId(@PathVariable String arquivoId) {
        try {
            UUID id = UUID.fromString(arquivoId);
            
            return pdfQuestaoRepository.findById(id)
                    .map(entity -> {
                        PdfQuestaoDTO dto = new PdfQuestaoDTO();
                        dto.setId(entity.getId().toString());
                        dto.setNomeOriginal(entity.getNomeOriginal());
                        dto.setContentType(entity.getContentType());
                        dto.setTamanhoBytes(entity.getTamanhoBytes());
                        dto.setDataUpload(entity.getDataUpload());
                        
                        if (entity.getPromptUtilizado() != null) {
                            dto.setPromptId(entity.getPromptUtilizado().getId().toString());
                        }
                        
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
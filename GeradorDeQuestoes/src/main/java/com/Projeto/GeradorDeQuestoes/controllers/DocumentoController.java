package com.Projeto.GeradorDeQuestoes.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Projeto.GeradorDeQuestoes.repositories.VectorStoreRepository;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentoController {

    private final VectorStoreRepository vectorStoreRepository;

    public DocumentoController( VectorStoreRepository vectorStoreRepository) {
        this.vectorStoreRepository = vectorStoreRepository;
    }

    @GetMapping("/{documentoId}/conceitos")
    public ResponseEntity<List<String>> listarConceitosPorDocumento(@PathVariable String documentoId) {
        List<String> conceitos = vectorStoreRepository.findDistinctConceitosByDocumentoId(documentoId);
        return ResponseEntity.ok(conceitos);
    }
}
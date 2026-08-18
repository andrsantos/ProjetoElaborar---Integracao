package com.Projeto.GeradorDeQuestoes.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.Projeto.GeradorDeQuestoes.dto.CenarioConfigDTO;
import com.Projeto.GeradorDeQuestoes.dto.FonteReferenciaDTO;
import com.Projeto.GeradorDeQuestoes.dto.TopicoConfigDTO;
import com.Projeto.GeradorDeQuestoes.entities.CenarioConfigEntity;
import com.Projeto.GeradorDeQuestoes.entities.DocumentoPromptEntity;
import com.Projeto.GeradorDeQuestoes.entities.DocumentosReferenciaEntity;
import com.Projeto.GeradorDeQuestoes.entities.PdfQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.PromptEntity;
import com.Projeto.GeradorDeQuestoes.entities.TopicoConfigEntity;
import com.Projeto.GeradorDeQuestoes.repositories.BancoQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.CenarioConfigRepository;
import com.Projeto.GeradorDeQuestoes.repositories.DocumentoPromptRepository;
import com.Projeto.GeradorDeQuestoes.repositories.DocumentosReferenciaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.PromptRepository;
import com.Projeto.GeradorDeQuestoes.repositories.TopicoConfigRepository;
import com.Projeto.GeradorDeQuestoes.repositories.VectorStoreRepository;
import com.Projeto.GeradorDeQuestoes.services.GerenciamentoService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class GerenciamentoServiceImpl implements GerenciamentoService {

    private final TopicoConfigRepository topicoConfigRepository;
    private final CenarioConfigRepository cenarioConfigRepository;
    private final VectorStoreRepository vectorStoreRepository;
    private final DocumentosReferenciaRepository documentosReferenciaRepository;
    private final DocumentoPromptRepository documentosPromptRepository;
    private final PromptRepository promptRepository;
    private final BancoQuestaoRepository bancoQuestaoRepository; // Injetado para buscar provas


    public GerenciamentoServiceImpl(TopicoConfigRepository topicoConfigRepository, CenarioConfigRepository cenarioConfigRepository,
        VectorStoreRepository vectorStoreRepository, 
        DocumentosReferenciaRepository documentosReferenciaRepository, 
        DocumentoPromptRepository documentosPromptRepository,
        PromptRepository promptRepository,
        BancoQuestaoRepository bancoQuestaoRepository
    ) {
        this.topicoConfigRepository = topicoConfigRepository;
        this.cenarioConfigRepository = cenarioConfigRepository;
        this.vectorStoreRepository = vectorStoreRepository;
        this.documentosReferenciaRepository = documentosReferenciaRepository;
        this.documentosPromptRepository = documentosPromptRepository;
        this.promptRepository = promptRepository;
        this.bancoQuestaoRepository = bancoQuestaoRepository;
    }
    
    /** OPERAÇÕES CRUD DE PROMPTS **/
    @Override
    public List<TopicoConfigEntity> listarTopicos() {
        return  topicoConfigRepository.findAll();
    }

    @Override
    public TopicoConfigDTO criarTopico(TopicoConfigDTO topicoConfigDTO) {

        TopicoConfigEntity topicoConfigEntity = new TopicoConfigEntity();
        topicoConfigEntity.setTopico(topicoConfigDTO.getTopico());
        topicoConfigEntity.setNivel(topicoConfigDTO.getNivel());
        topicoConfigEntity.setInstrucoesEspecificas(topicoConfigDTO.getInstrucoesEspecificas());
        topicoConfigEntity.setDataAtualizacao(LocalDateTime.now());
        TopicoConfigEntity savedEntity = topicoConfigRepository.save(topicoConfigEntity);

        TopicoConfigDTO savedDTO = new TopicoConfigDTO();
        savedDTO.setId(savedEntity.getId());
        savedDTO.setTopico(savedEntity.getTopico());
        savedDTO.setNivel(savedEntity.getNivel());
        savedDTO.setInstrucoesEspecificas(savedEntity.getInstrucoesEspecificas());
        savedDTO.setDataAtualizacao(LocalDateTime.now());

        
        topicoConfigDTO.getListaDocumentos().forEach(documento -> {
        DocumentoPromptEntity documentoPrompt = new DocumentoPromptEntity();
        documentoPrompt.setIdDocumento(documento);
        documentoPrompt.setIdPrompt(savedEntity.getId());
        documentosPromptRepository.save(documentoPrompt);
        });

        return savedDTO;
    }

    @Override
    public void deletarTopico(String id) {
        topicoConfigRepository.deleteById(id);
    }

    @Override
    public TopicoConfigDTO atualizarTopico(String id, TopicoConfigDTO topicoConfigDTO) {
        TopicoConfigEntity topicoConfigEntity = topicoConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tópico não encontrado com ID: " + id));

        topicoConfigEntity.setTopico(topicoConfigDTO.getTopico());
        topicoConfigEntity.setNivel(topicoConfigDTO.getNivel());
        topicoConfigEntity.setInstrucoesEspecificas(topicoConfigDTO.getInstrucoesEspecificas());

        TopicoConfigEntity atualizado = topicoConfigRepository.save(topicoConfigEntity);

        TopicoConfigDTO atualizadoDTO = new TopicoConfigDTO();
        atualizadoDTO.setId(atualizado.getId());
        atualizadoDTO.setTopico(atualizado.getTopico());
        atualizadoDTO.setNivel(atualizado.getNivel());
        atualizadoDTO.setInstrucoesEspecificas(atualizado.getInstrucoesEspecificas());

        return atualizadoDTO;
    }

    /** OPERAÇÕES CRUD DE CENÁRIOS **/
    @Override
    public List<CenarioConfigEntity> listarCenarios() {
        return cenarioConfigRepository.findAll();
    }

    @Override
    public CenarioConfigDTO criarCenario(CenarioConfigDTO cenarioConfigDTO) {
        CenarioConfigEntity cenarioConfigEntity = new CenarioConfigEntity();
        cenarioConfigEntity.setTopico(cenarioConfigDTO.getTopico());
        cenarioConfigEntity.setNivel(cenarioConfigDTO.getNivel());
        cenarioConfigEntity.setDescricao(cenarioConfigDTO.getDescricao());
        CenarioConfigEntity savedEntity = cenarioConfigRepository.save(cenarioConfigEntity);
        CenarioConfigDTO savedDTO = new CenarioConfigDTO();
        savedDTO.setId(savedEntity.getId());
        savedDTO.setTopico(savedEntity.getTopico());
        savedDTO.setNivel(savedEntity.getNivel());
        savedDTO.setDescricao(savedEntity.getDescricao());
        return savedDTO;
    }

    @Override
    public void deletarCenario(Long id) {
        cenarioConfigRepository.deleteById(id);
    }

    @Override
    public CenarioConfigDTO atualizarCenario(Long id, CenarioConfigDTO cenarioConfigDTO) {
        CenarioConfigEntity cenarioConfigEntity = cenarioConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cenário não encontrado com ID: " + id));

        cenarioConfigEntity.setTopico(cenarioConfigDTO.getTopico());
        cenarioConfigEntity.setNivel(cenarioConfigDTO.getNivel());
        cenarioConfigEntity.setDescricao(cenarioConfigDTO.getDescricao());

        CenarioConfigEntity atualizado = cenarioConfigRepository.save(cenarioConfigEntity);

        CenarioConfigDTO atualizadoDTO = new CenarioConfigDTO();
        atualizadoDTO.setId(atualizado.getId());
        atualizadoDTO.setTopico(atualizado.getTopico());
        atualizadoDTO.setNivel(atualizado.getNivel());
        atualizadoDTO.setDescricao(atualizado.getDescricao());

        return atualizadoDTO;
    }

    /** OPERAÇÕES CRUD DE DOCUMENTOS **/


    @Override
    @Transactional
    public void deletarDocumento(String documentoId) {
        DocumentosReferenciaEntity documento = documentosReferenciaRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado com o ID: " + documentoId));


        try {
            vectorStoreRepository.deleteByDocumentoId(documentoId);
            System.out.println("🧠 Memória limpa: Fragmentos vetoriais removidos para o documento " + documentoId);
        } catch (Exception e) {
            System.err.println("⚠️ Aviso: Falha ao limpar o banco vetorial. Detalhe: " + e.getMessage());
        }

        documentosReferenciaRepository.delete(documento);

        System.out.println("🗑️ Exclusão concluída: Documento [" + documento.getTitulo() + "] removido.");
    }


    @Override
    public List<Map<String, Object>> listarDocumentosFiltrados(String disciplinaId) {
        List<DocumentosReferenciaEntity> documentos = documentosReferenciaRepository.findByDisciplinaId(disciplinaId);
        
        return documentos.stream().map(doc -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", doc.getId());
            dto.put("titulo", doc.getTitulo());
            
            if (doc.getPdfBinario() != null) {
                dto.put("idBinario", doc.getPdfBinario().getId());
                dto.put("nomeArquivo", doc.getPdfBinario().getNomeOriginal());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<FonteReferenciaDTO> listarFontesReferencia(String disciplinaId) {
        List<FonteReferenciaDTO> fontes = new ArrayList<>();

        List<DocumentosReferenciaEntity> documentos = documentosReferenciaRepository.findByDisciplinaId(disciplinaId);
        for (DocumentosReferenciaEntity doc : documentos) {
            fontes.add(new FonteReferenciaDTO(
                doc.getId().toString(),
                doc.getTitulo(),
                "DOCUMENTO"
            ));
        }


        List<PdfQuestaoEntity> provas = bancoQuestaoRepository.findProvasUnicasPorDisciplina(disciplinaId);
        
        for (PdfQuestaoEntity prova : provas) {
            if (prova != null) {
                fontes.add(new FonteReferenciaDTO(
                    prova.getId().toString(),  
                    prova.getNomeOriginal(),    
                    "PROVA"
                ));
            }
        }

        return fontes;
    }
}
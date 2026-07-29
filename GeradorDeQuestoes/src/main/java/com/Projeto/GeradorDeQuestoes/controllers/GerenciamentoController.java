package com.Projeto.GeradorDeQuestoes.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Projeto.GeradorDeQuestoes.dto.CenarioConfigDTO;
import com.Projeto.GeradorDeQuestoes.dto.FiltroGerenciamentoDTO;
import com.Projeto.GeradorDeQuestoes.dto.ResultadoIngestaoDTO;
import com.Projeto.GeradorDeQuestoes.dto.TopicoConfigDTO;
import com.Projeto.GeradorDeQuestoes.entities.DocumentosReferenciaEntity;
import com.Projeto.GeradorDeQuestoes.entities.PdfBinarioEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.services.CarteiraService;
import com.Projeto.GeradorDeQuestoes.services.CobrancaLlmService;
import com.Projeto.GeradorDeQuestoes.services.DocumentosReferenciaService;
import com.Projeto.GeradorDeQuestoes.services.GerenciamentoService;
import com.Projeto.GeradorDeQuestoes.services.PdfBinarioService;
import com.Projeto.GeradorDeQuestoes.services.VectorIngestionService;


@RestController
@RequestMapping("/api/gerenciamento")
@CrossOrigin(origins = "http://localhost:4200")
public class GerenciamentoController {

    
    private final GerenciamentoService gerenciamentoService;
    private final PdfBinarioService pdfBinarioService;
    private final DocumentosReferenciaService documentosReferenciaService;
    private final VectorIngestionService ingestionService;
    private final CarteiraService carteiraService;


    public GerenciamentoController(GerenciamentoService gerenciamentoService, 
        PdfBinarioService pdfBinarioService, 
        DocumentosReferenciaService documentosReferenciaService, 
        VectorIngestionService ingestionService, 
        CarteiraService carteiraService, 
        CobrancaLlmService cobrancaLlmService
        ) {
        this.gerenciamentoService = gerenciamentoService;
        this.pdfBinarioService = pdfBinarioService;
        this.documentosReferenciaService = documentosReferenciaService;
        this.ingestionService = ingestionService;
        this.carteiraService = carteiraService;
    }

    @PostMapping("/listar")
    public List<?> listarGerenciamento(@RequestBody FiltroGerenciamentoDTO filtro) {

        System.out.println("Filtro recebido: " + filtro.getFiltro().name());

        switch (filtro.getFiltro()) {
            case PROMPTS:
                return gerenciamentoService.listarTopicos();
            case CENARIO:
                return gerenciamentoService.listarCenarios();
            default:
                throw new IllegalArgumentException("Filtro inválido: " + filtro.getFiltro());
        }

    }
    

    // ****** OPERAÇÕES CRUD PARA CENÁRIOS ****** //
    @PostMapping("/criar/cenario")
    public CenarioConfigDTO criarCenario(@RequestBody CenarioConfigDTO cenarioConfigDTO) {
       return gerenciamentoService.criarCenario(cenarioConfigDTO);
    }

    @DeleteMapping("/deletar/cenario/{id}")
    public void deletarCenario(@PathVariable Long id) {
        System.out.println("ID recebido para deleção: " + id);
        gerenciamentoService.deletarCenario(id);
    }

    @PutMapping("/atualizar/cenario/{id}")
    public CenarioConfigDTO atualizarCenario(@PathVariable Long id, @RequestBody CenarioConfigDTO cenarioConfigDTO) {
        return gerenciamentoService.atualizarCenario(id, cenarioConfigDTO);
    }

    // ****** OPERAÇÕES CRUD PARA PROMPTS ****** //
    @PostMapping("/criar/prompt")
    public TopicoConfigDTO criarPrompts(@RequestBody TopicoConfigDTO topicoConfigDTO) {
       return gerenciamentoService.criarTopico(topicoConfigDTO);
    }

    @DeleteMapping("/deletar/prompt/{id}")
    public void deletarPrompt(@PathVariable String id) {
        System.out.println("ID recebido para deleção: " + id);
        gerenciamentoService.deletarTopico(id);
    }

    @PutMapping("/atualizar/prompt/{id}")
    public TopicoConfigDTO atualizarPrompt(@PathVariable String id, @RequestBody TopicoConfigDTO topicoConfigDTO) {
        System.out.println("ID recebido para atualização: " + id);
        return gerenciamentoService.atualizarTopico(id, topicoConfigDTO);
    }

    // ****** OPERAÇÕES CRUD PARA DOCUMENTOS ****** //
   
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titulo") String titulo,
            @RequestParam("disciplinaId") String disciplinaId,
        @AuthenticationPrincipal UsuarioEntity usuario) { 

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Arquivo vazio"));
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Apenas arquivos PDF são aceitos"));
        }

        try {
            byte[] bytes = file.getBytes();
            
            PdfBinarioEntity pdfBinario = pdfBinarioService.salvarOuRecuperar(bytes, filename);

            DocumentosReferenciaEntity docReferencia = documentosReferenciaService.vincularContexto(
                pdfBinario, titulo, disciplinaId
            );

            Map<String, Object> metadata = new HashMap<>(); 
            metadata.put("titulo_documento", titulo);
            metadata.put("arquivo_original", filename);
            metadata.put("documento_id", docReferencia.getId().toString()); 
            metadata.put("disciplina_id", disciplinaId); 

            ResultadoIngestaoDTO chunks = ingestionService.ingerirPdf(bytes, filename, metadata, usuario);

            return ResponseEntity.ok(Map.of(
                "mensagem", "PDF indexado e vinculado com sucesso",
                "id_binario", pdfBinario.getId(),
                "id_referencia", docReferencia.getId(),
                "arquivo", filename,
                "chunks_inseridos", chunks
            ));

        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(Map.of("erro", "Parâmetro inválido: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("erro", "Falha ao processar PDF: " + e.getMessage()));
        }
    }

    @DeleteMapping("/deletar/documento/{id}")
    public ResponseEntity<Void> deletarDocumento(@PathVariable String id) {
        System.out.println("ID recebido para deleção de documento: " + id);
        
        gerenciamentoService.deletarDocumento(id);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listar/documentos/filtrados")
    public ResponseEntity<?> listarDocumentosFiltrados(@RequestParam("disciplinaId") String disciplinaId) {
        System.out.println("Buscando documentos para a disciplina: " + disciplinaId);
        return ResponseEntity.ok(gerenciamentoService.listarDocumentosFiltrados(disciplinaId));
    }

    @GetMapping("/listar/fontes-referencia")
    public ResponseEntity<?> listarFontesReferenciaParaGeracao(@RequestParam("disciplinaId") String disciplinaId) {
        System.out.println("Buscando fontes unificadas (Docs + Provas) para a disciplina: " + disciplinaId);
        
        return ResponseEntity.ok(gerenciamentoService.listarFontesReferencia(disciplinaId));
    }



}

package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.dto.LoteQuestoesDTO;
import com.Projeto.GeradorDeQuestoes.dto.QuestaoComOrigemDTO;
import com.Projeto.GeradorDeQuestoes.dto.QuestaoDTO;
import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.entities.PdfQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.enums.NivelTecnico;
import com.Projeto.GeradorDeQuestoes.enums.TipoQuestao;
import com.Projeto.GeradorDeQuestoes.repositories.BancoQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.ExtracaoJobRepository;
import com.Projeto.GeradorDeQuestoes.repositories.PdfQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.services.BancoQuestaoService;
import com.Projeto.GeradorDeQuestoes.services.ConceitoService;
import com.Projeto.GeradorDeQuestoes.services.PdfQuestaoService;
import com.Projeto.GeradorDeQuestoes.services.VectorIngestionService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/banco-questoes")
@CrossOrigin(origins = "http://localhost:4200")
public class BancoQuestaoController {

    private final BancoQuestaoRepository repository;
    private final BancoQuestaoService bancoService;
    private final PdfQuestaoRepository pdfQuestaoRepository;
    private final VectorIngestionService vetorizacaoService;
    private final ConceitoService conceitoService;
    private final ExtracaoJobRepository jobRepository;
    private final PdfQuestaoService pdfQuestaoService;


    public BancoQuestaoController(BancoQuestaoRepository repository, 
        PdfQuestaoRepository pdfQuestaoRepository, 
        VectorIngestionService vetorizacaoService, 
        ConceitoService conceitoService, 
        BancoQuestaoService bancoService, 
        ExtracaoJobRepository jobRepository, 
        PdfQuestaoService pdfQuestaoService) {
        this.repository = repository;
        this.bancoService = bancoService;
        this.pdfQuestaoRepository = pdfQuestaoRepository;
        this.vetorizacaoService = vetorizacaoService;
        this.conceitoService = conceitoService;
        this.jobRepository = jobRepository;
        this.pdfQuestaoService = pdfQuestaoService;
    }

    @PostMapping
    public ResponseEntity<BancoQuestaoEntity> criarQuestao(@RequestBody QuestaoDTO questao) {

        BancoQuestaoEntity novaQuestao = new BancoQuestaoEntity();
        novaQuestao.setEnunciado(questao.getEnunciado());
        novaQuestao.setAlternativas(questao.getAlternativas());
        novaQuestao.setRespostaCorreta(questao.getRespostaCorreta());
        novaQuestao.setConceito(questao.getConceito());
        novaQuestao.setCompetencia(questao.getCompetencia());
        novaQuestao.setComentarioTecnico(questao.getComentarioTecnico());
        novaQuestao.setTipo(TipoQuestao.MULTIPLA_ESCOLHA_5);
        BancoQuestaoEntity salva = repository.save(novaQuestao);
        return ResponseEntity.ok(salva);

    }

    @PostMapping("/cadastrar/pdf/upload")
    public ResponseEntity<PdfQuestaoEntity> cadastrarPdfUpload(
        @RequestParam("file") MultipartFile file) throws IOException {

        PdfQuestaoEntity entidade = new PdfQuestaoEntity();
        entidade.setNomeOriginal(file.getOriginalFilename());
        entidade.setNomeArmazenamento(UUID.randomUUID() + "_" + file.getOriginalFilename());
        entidade.setConteudo(file.getBytes()); 
        entidade.setContentType(file.getContentType());
        entidade.setTamanhoBytes(file.getSize());
        return ResponseEntity.ok(pdfQuestaoRepository.save(entidade));

    }

    @GetMapping("/cadastrar/pdf/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {

        PdfQuestaoEntity pdf = pdfQuestaoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + pdf.getNomeOriginal() + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf.getConteudo());

    }
    
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarQuestao(@RequestBody QuestaoComOrigemDTO dto, 
    @AuthenticationPrincipal UsuarioEntity usuario) {

    System.out.println("=== DTO RECEBIDO ===");
    System.out.println("tipo: [" + dto.getTipo() + "]");
    System.out.println("nivel: [" + dto.getNivel() + "]");
    System.out.println("enunciado: [" + dto.getEnunciado() + "]");
    System.out.println("origem: [" + dto.getOrigem() + "]");
    System.out.println("===================");

    try {

        BancoQuestaoEntity questao = new BancoQuestaoEntity();
        questao.setDisciplinaId(dto.getDisciplinaId());
        questao.setEnunciado(dto.getEnunciado());
        questao.setAlternativas(dto.getAlternativas());
        questao.setRespostaCorreta(dto.getRespostaCorreta());
        questao.setConceito(dto.getConceito());
        questao.setComentarioTecnico(dto.getComentarioTecnico());
        questao.setCompetencia(dto.getCompetencia());
        questao.setTipo(dto.getTipo() != null ? TipoQuestao.valueOf(dto.getTipo()) : TipoQuestao.MULTIPLA_ESCOLHA_5);
        questao.setNivel(dto.getNivel() != null ? NivelTecnico.valueOf(dto.getNivel()) : null);

        if (dto.getDataCriacao() != null) questao.setDataCriacao(LocalDateTime.parse(dto.getDataCriacao()));
    
        if(dto.getOrigem().equals("GERADO_POR_DOCUMENTO")){
            questao.setArquivoOrigem(null);
            questao.setTipoDocumento(BancoQuestaoEntity.TipoDocumento.DOCUMENTO);
        }

        if(dto.getOrigem().equals("GERADO_POR_PROVA")){
            System.out.println("Buscando arquivo de origem com ID: " + dto.getArquivoOrigem());
            if (dto.getArquivoOrigem() != null && !dto.getArquivoOrigem().isBlank()) {
                pdfQuestaoRepository.findById(UUID.fromString(dto.getArquivoOrigem()))
                        .ifPresent(questao::setArquivoOrigem);
            }
            questao.setTipoDocumento(BancoQuestaoEntity.TipoDocumento.PROVA);
        }


        if (dto.getDisciplinaId() != null) {
        String conceitoOriginal = dto.getConceito();
        List<String> conceitosExistentes = conceitoService.listarConceitosPorDisciplina(dto.getDisciplinaId());
        
        String conceitoNormalizado = bancoService.normalizarConceito(
            dto.getEnunciado(),
            conceitoOriginal,
            conceitosExistentes,
            usuario
        );
        
        questao.setConceito(conceitoNormalizado);
        System.out.println("🔄 Conceito Normalizado: de [" + conceitoOriginal + "] para [" + conceitoNormalizado + "]");
        } else {
            questao.setConceito(dto.getConceito());
        }
        




        BancoQuestaoEntity questaoSalva = repository.save(questao);
            if(questaoSalva.getTipoDocumento() == BancoQuestaoEntity.TipoDocumento.PROVA) {
                try {
                    vetorizacaoService.vetorizarQuestaoDeProva(questaoSalva);
                    System.out.println("Questão de prova vetorizada com sucesso: " + questaoSalva.getId());
                } catch (Exception e) {
                    System.err.println("Aviso: Questão salva no DB, mas falhou ao vetorizar. " + e.getMessage());
                }
            }

            return ResponseEntity.ok(questaoSalva);

    } catch (Exception e) {

        System.out.println("ERRO AO SALVAR: " + e.getMessage());
        return ResponseEntity.status(500).body("Erro: " + e.getMessage() + " | DTO tipo=" + dto.getTipo() + " nivel=" + dto.getNivel());
    
    }
    }


    @PostMapping("/cadastrar-lote")
    @Transactional 
    public ResponseEntity<?> cadastrarLoteDeQuestoes(@RequestBody LoteQuestoesDTO payload,
    @AuthenticationPrincipal UsuarioEntity usuario
    ) {
        
        System.out.println("=== RECEBENDO LOTE DE QUESTÕES ===");
        System.out.println("Job ID associado: " + payload.getJobId());
        System.out.println("Total de questões: " + (payload.getQuestoes() != null ? payload.getQuestoes().size() : 0));
        System.out.println("==================================");

        String disciplinaId = payload.getQuestoes().get(0).getDisciplinaId();
        System.out.println("Disciplina ID: " + disciplinaId);

        try {
            PdfQuestaoEntity pdfSalvo = null;

            if (payload.getJobId() != null && !payload.getJobId().isBlank()) {

                    ExtracaoJobEntity job = jobRepository.findById(payload.getJobId())
                        .orElseThrow(() -> new RuntimeException("Job não encontrado para o ID: " + payload.getJobId())); 

                if (job != null && job.getCaminhoArquivoTemporario() != null) {
                    
                    Path caminhoTemp = Paths.get(job.getCaminhoArquivoTemporario());

                    if (Files.exists(caminhoTemp)) {
                        PdfQuestaoEntity novoPdf = new PdfQuestaoEntity();
                        novoPdf.setNomeOriginal(job.getNomeArquivo());
                        novoPdf.setNomeArmazenamento(UUID.randomUUID() + "_" + job.getNomeArquivo());
                        novoPdf.setConteudo(Files.readAllBytes(caminhoTemp));
                        novoPdf.setContentType("application/pdf");
                        novoPdf.setDisciplinaId(disciplinaId);
                        novoPdf.setTamanhoBytes(Files.size(caminhoTemp));
                     
                        pdfSalvo = pdfQuestaoRepository.save(novoPdf);
                        System.out.println("✅ PDF salvo definitivamente no banco com ID: " + pdfSalvo.getId());

                        Files.delete(caminhoTemp);
                        System.out.println("🧹 Arquivo temporário removido do disco.");
                    } else {
                        System.err.println("⚠️ Arquivo temporário não encontrado no disco: " + caminhoTemp);
                    }
                }
            }

            List<BancoQuestaoEntity> questoesParaSalvar = new ArrayList<>();

            for (QuestaoComOrigemDTO dto : payload.getQuestoes()) {
                BancoQuestaoEntity questao = new BancoQuestaoEntity();
                
                questao.setDisciplinaId(dto.getDisciplinaId());
                questao.setEnunciado(dto.getEnunciado());
                questao.setAlternativas(dto.getAlternativas());
                questao.setRespostaCorreta(dto.getRespostaCorreta());
                questao.setComentarioTecnico(dto.getComentarioTecnico());
                questao.setCompetencia(dto.getCompetencia());
                questao.setTipo(dto.getTipo() != null ? TipoQuestao.valueOf(dto.getTipo()) : TipoQuestao.MULTIPLA_ESCOLHA_5);
                questao.setNivel(dto.getNivel() != null ? NivelTecnico.valueOf(dto.getNivel()) : null);

                if (dto.getDataCriacao() != null) {
                    questao.setDataCriacao(LocalDateTime.parse(dto.getDataCriacao()));
                }
            
                if ("GERADO_POR_DOCUMENTO".equals(dto.getOrigem())) {
                    questao.setArquivoOrigem(null);
                    questao.setTipoDocumento(BancoQuestaoEntity.TipoDocumento.DOCUMENTO);
                } else if ("GERADO_POR_PROVA".equals(dto.getOrigem())) {
                    if (pdfSalvo != null) {
                        questao.setArquivoOrigem(pdfSalvo);
                    }
                    questao.setTipoDocumento(BancoQuestaoEntity.TipoDocumento.PROVA);
                }

                if (dto.getDisciplinaId() != null) {
                    String conceitoOriginal = dto.getConceito();
                    List<String> conceitosExistentes = conceitoService.listarConceitosPorDisciplina(dto.getDisciplinaId());
                    String conceitoNormalizado = bancoService.normalizarConceito(
                        dto.getEnunciado(), conceitoOriginal, conceitosExistentes, usuario
                    );
                    questao.setConceito(conceitoNormalizado);
                } else {
                    questao.setConceito(dto.getConceito());
                }

                questoesParaSalvar.add(questao);
            }

            List<BancoQuestaoEntity> questoesSalvas = repository.saveAll(questoesParaSalvar);
            System.out.println("✅ " + questoesSalvas.size() + " questões cadastradas com sucesso.");

            for (BancoQuestaoEntity questaoSalva : questoesSalvas) {
                if(questaoSalva.getTipoDocumento() == BancoQuestaoEntity.TipoDocumento.PROVA) {
                    try {
                        vetorizacaoService.vetorizarQuestaoDeProva(questaoSalva);
                    } catch (Exception e) {
                        System.err.println("⚠️ Aviso: Questão salva no DB, mas falhou ao vetorizar. ID: " + questaoSalva.getId() + " - " + e.getMessage());
                    }
                }
            }

            return ResponseEntity.ok(questoesSalvas);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ ERRO CRÍTICO AO SALVAR LOTE: " + e.getMessage());
            return ResponseEntity.status(500).body("Erro interno ao processar lote: " + e.getMessage());
        }
    }




    @GetMapping
    public ResponseEntity<List<BancoQuestaoEntity>> listarTodas() {

        return ResponseEntity.ok(repository.findAll());

    }

    @GetMapping("/listar/{disciplinaId}")
    public ResponseEntity<List<BancoQuestaoEntity>> listarTodasPorDisciplina(@PathVariable String disciplinaId) {
        
        System.out.println("Buscando questões para a disciplina:" + disciplinaId);

        return ResponseEntity.ok(repository.findByDisciplinaId(disciplinaId));

    }

    @GetMapping("/{id}")
    public ResponseEntity<BancoQuestaoEntity> buscarPorId(@PathVariable UUID id) {

        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }


    @PutMapping("/{id}")
    public ResponseEntity<BancoQuestaoEntity> atualizarQuestao(@PathVariable UUID id, @RequestBody BancoQuestaoEntity questaoAtualizada) {
        
        Optional<BancoQuestaoEntity> questaoExistenteOpt = repository.findById(id);

        if (questaoExistenteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BancoQuestaoEntity questaoOriginal = questaoExistenteOpt.get();

        questaoOriginal.setEnunciado(questaoAtualizada.getEnunciado());
        questaoOriginal.setAlternativas(questaoAtualizada.getAlternativas());
        questaoOriginal.setRespostaCorreta(questaoAtualizada.getRespostaCorreta());
        questaoOriginal.setComentarioTecnico(questaoAtualizada.getComentarioTecnico());
        

        BancoQuestaoEntity salva = repository.save(questaoOriginal);
        
        return ResponseEntity.ok(salva);
    }

    

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirQuestao(@PathVariable UUID id) {

        if (!repository.existsById(id)) {

            return ResponseEntity.notFound().build();

        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
        
    }

    @GetMapping("/disciplina/{disciplinaId}/conceitos")
    public ResponseEntity<List<String>> listarConceitosDaDisciplina(@PathVariable String disciplinaId) {
        List<String> conceitos = conceitoService.listarConceitosPorDisciplina(disciplinaId);
        return ResponseEntity.ok(conceitos);
    }

    @GetMapping("/listar-provas/{provaId}/questoes")
    public ResponseEntity<?> listarQuestoesDaProva(@PathVariable UUID provaId) {
        System.out.println("Buscando questões vinculadas à prova ID: " + provaId);
        
        try {
            List<BancoQuestaoEntity> questoes = pdfQuestaoService.buscarQuestoesPorProvaId(provaId);
            
            if (questoes.isEmpty()) {
                return ResponseEntity.noContent().build(); 
            }
            
            return ResponseEntity.ok(questoes);
        } catch (Exception e) {
            System.err.println("Erro ao buscar questões da prova: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erro ao buscar as questões: " + e.getMessage());
        }
    }



}
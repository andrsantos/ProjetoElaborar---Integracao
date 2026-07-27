package com.Projeto.GeradorDeQuestoes.controllers;

import com.Projeto.GeradorDeQuestoes.dto.Prova;
import com.Projeto.GeradorDeQuestoes.dto.ProvaInfoDTO;
import com.Projeto.GeradorDeQuestoes.dto.Questao;
import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.DisciplinaEntity;
import com.Projeto.GeradorDeQuestoes.entities.ProvaEntity;
import com.Projeto.GeradorDeQuestoes.entities.QuestaoProvaEntity;
import com.Projeto.GeradorDeQuestoes.enums.TipoQuestao;
import com.Projeto.GeradorDeQuestoes.repositories.BancoQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.repositories.ProvaRepository;
import com.Projeto.GeradorDeQuestoes.repositories.QuestaoProvaRepository;
import com.Projeto.GeradorDeQuestoes.services.DisciplinaService;
import com.Projeto.GeradorDeQuestoes.services.GeradorQuestaoService;
import com.Projeto.GeradorDeQuestoes.services.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provas-salvas")
@CrossOrigin(origins = "http://localhost:4200") 
public class ProvaSalvaController {

    private final ProvaRepository provaRepository;
    private final PdfService pdfService;
    private final QuestaoProvaRepository questaoProvaRepository;
    private final DisciplinaService disciplinaService;
    private final BancoQuestaoRepository bancoQuestaoRepository;
    private final GeradorQuestaoService geradorQuestaoService;
    



    public ProvaSalvaController(ProvaRepository provaRepository, 
        PdfService pdfService,
        QuestaoProvaRepository questaoProvaRepository, 
        DisciplinaService disciplinaService, 
        BancoQuestaoRepository bancoQuestaoRepository, 
        GeradorQuestaoService geradorQuestaoService) {
        this.provaRepository = provaRepository;
        this.pdfService = pdfService;
        this.questaoProvaRepository = questaoProvaRepository;
        this.disciplinaService = disciplinaService;
        this.bancoQuestaoRepository = bancoQuestaoRepository;
        this.geradorQuestaoService = geradorQuestaoService;
    }

    @PostMapping
    public ResponseEntity<ProvaEntity> salvarProva(@RequestBody Prova provaDto) {
        
        ProvaEntity novaProva = new ProvaEntity();
        DisciplinaEntity disciplina = disciplinaService.buscarPorId(provaDto.getDisciplinaId()).orElseThrow();
        novaProva.setDisciplina(disciplina);
        
        String titulo = "Prova Gerada - " + java.time.LocalDate.now().toString();
        novaProva.setTitulo(titulo);

        if (provaDto.getQuestoes() != null && !provaDto.getQuestoes().isEmpty()) {
            
            for (Questao questaoDto : provaDto.getQuestoes()) {
                QuestaoProvaEntity questaoEntity = new QuestaoProvaEntity();
                
                questaoEntity.setEnunciado(questaoDto.getEnunciado());
                questaoEntity.setAlternativas(questaoDto.getAlternativas());
                questaoEntity.setRespostaCorreta(questaoDto.getRespostaCorreta());
                questaoEntity.setComentarioTecnico(questaoDto.getComentarioTecnico());
                questaoEntity.setConceito(questaoDto.getConceito());
                questaoEntity.setCompetencia(questaoDto.getCompetencia());
                questaoEntity.setNivel(questaoDto.getNivel().toString());
                novaProva.addQuestao(questaoEntity); 
            }
        }

        ProvaEntity provaSalva = provaRepository.save(novaProva);

        System.out.println("CONTROLLER: Prova salva com sucesso! ID: " + provaSalva.getId());

        return ResponseEntity.ok(provaSalva);
    }

    @GetMapping
    public ResponseEntity<List<ProvaInfoDTO>> getListaProvas() {
        List<ProvaInfoDTO> provas = provaRepository.findAllWithInfo();
        return ResponseEntity.ok(provas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvaEntity> getDetalheProva(@PathVariable UUID id) {
        return provaRepository.findById(id)
                .map(ResponseEntity::ok) 
                .orElse(ResponseEntity.notFound().build()); 
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirProva(@PathVariable UUID id) {
        if (!provaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        provaRepository.deleteById(id);
        System.out.println("CONTROLLER: Prova " + id + " excluída.");
        return ResponseEntity.noContent().build(); 
    }

    @PutMapping("/questoes/{idQuestao}")
    public ResponseEntity<QuestaoProvaEntity> atualizarQuestao(
            @PathVariable UUID idQuestao,
            @RequestBody Questao questaoDto) {
        
        return questaoProvaRepository.findById(idQuestao)
            .map(entity -> {
                entity.setEnunciado(questaoDto.getEnunciado());
                entity.setAlternativas(questaoDto.getAlternativas());
                entity.setRespostaCorreta(questaoDto.getRespostaCorreta());
                QuestaoProvaEntity saved = questaoProvaRepository.save(entity);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/download-pdf")
    public ResponseEntity<byte[]> baixarProvaPdf(@PathVariable UUID id) {
        
        ProvaEntity provaEntity = provaRepository.findById(id).orElse(null);

        if (provaEntity == null) {

            return ResponseEntity.notFound().build();

        }

        try {

            Prova provaDto = convertEntityToDto(provaEntity);
            byte[] pdfBytes = pdfService.gerarPdfProva(provaDto);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "prova_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (IOException e) {

            e.printStackTrace();
            return ResponseEntity.internalServerError().build();

        }
    }

  
    private Prova convertEntityToDto(ProvaEntity entity) {
        
        Prova provaDto = new Prova(); 
        
        List<Questao> questoesDto = entity.getQuestoes().stream()
                .map(qe -> new Questao( 
                        qe.getId(),
                        qe.getEnunciado(),
                        qe.getAlternativas(),
                        qe.getRespostaCorreta(),
                        qe.getComentarioTecnico()
                ))
                .collect(Collectors.toList());

        questoesDto.forEach(provaDto::adicionarQuestao);
        
        return provaDto;
    }





    @PostMapping("/substituir-aleatoria")
    public ResponseEntity<?> substituirQuestaoAleatoria(@RequestBody Map<String, Object> payload) {
    
        String disciplinaId = (String) payload.get("disciplinaId");
        String conceito = (String) payload.get("conceito");
        
        @SuppressWarnings("unchecked")
        List<String> idsExcluidosStr = (List<String>) payload.get("idsExcluidos");
        
        List<UUID> idsExcluidos = idsExcluidosStr.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        long totalNoBanco = bancoQuestaoRepository.countByConceito(conceito); 
        
        if (idsExcluidos.size() >= totalNoBanco && totalNoBanco > 0) {
            System.out.println(">>> BANCO ESGOTADO para o conceito: " + conceito + ". Forçando 404 para acionar a IA.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Todas as questões do banco para este conceito já estão na prova."));
        }

        if (idsExcluidos.isEmpty()) {
            idsExcluidos.add(UUID.randomUUID());
        }

        Optional<BancoQuestaoEntity> novaQuestao = bancoQuestaoRepository.buscarQuestaoAleatoriaParaSubstituicao(
                disciplinaId, conceito, idsExcluidos
        );

        if (novaQuestao.isPresent()) {
            return ResponseEntity.ok(novaQuestao.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Nenhuma questão inédita encontrada para este conceito no banco."));
        }
    }

    @PostMapping("/gerar-substituta-ia")
    public ResponseEntity<?> gerarQuestaoSubstitutaIa(@RequestBody Map<String, String> payload) {
    
        String disciplinaId = payload.get("disciplinaId");
        String conceito = payload.get("conceito");
        String enunciadoAntigo = payload.get("enunciadoAntigo");
        String nivel = payload.getOrDefault("nivel", "MEDIO"); 
        
        System.out.println(">>> BANCO VAZIO! Acionando Agente Substituto para o conceito: " + conceito);

        try {

            Questao questaoGerada = geradorQuestaoService.gerarQuestaoSubstitutaAvulsa(conceito, enunciadoAntigo, nivel);

            BancoQuestaoEntity novaQuestao = new BancoQuestaoEntity();
            novaQuestao.setDisciplinaId(disciplinaId);
            novaQuestao.setConceito(questaoGerada.getConceito());
            novaQuestao.setEnunciado(questaoGerada.getEnunciado());
            novaQuestao.setRespostaCorreta(questaoGerada.getRespostaCorreta());
            novaQuestao.setComentarioTecnico(questaoGerada.getComentarioTecnico());
            novaQuestao.setNivel(questaoGerada.getNivel());
            novaQuestao.setCompetencia(questaoGerada.getCompetencia());
            novaQuestao.setDataCriacao(LocalDateTime.now());
            novaQuestao.setTipo(TipoQuestao.MULTIPLA_ESCOLHA_5);
            novaQuestao.setTopico("Não Classificado");
            novaQuestao.setAlternativas(questaoGerada.getAlternativas());
            bancoQuestaoRepository.save(novaQuestao);
            return ResponseEntity.ok(novaQuestao);
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar substituta com IA: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Falha ao gerar questão inédita com a IA."));
        }
    }

    @PostMapping("/catalogo-substituicao")
    public ResponseEntity<?> buscarCatalogoSubstituicao(@RequestBody Map<String, Object> payload) {
    
        String disciplinaIdStr = (String) payload.get("disciplinaId");
        String conceito = (String) payload.get("conceito");
        
        @SuppressWarnings("unchecked")
        List<String> idsExcluidosStr = (List<String>) payload.get("idsExcluidos");
        
        List<UUID> idsExcluidos = idsExcluidosStr.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        if (idsExcluidos.isEmpty()) {
            idsExcluidos.add(UUID.randomUUID());
        }

        try {
            String disciplinaId = disciplinaIdStr;
            
            List<BancoQuestaoEntity> catalogo = bancoQuestaoRepository.buscarCatalogoParaSubstituicao(
                    disciplinaId, conceito, idsExcluidos
            );

            return ResponseEntity.ok(catalogo);
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar catálogo de substituição: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Falha ao buscar questões no catálogo."));
        }
    }


}
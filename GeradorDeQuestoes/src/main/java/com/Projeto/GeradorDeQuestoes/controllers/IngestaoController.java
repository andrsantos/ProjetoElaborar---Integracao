package com.Projeto.GeradorDeQuestoes.controllers;

import java.io.File;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.Projeto.GeradorDeQuestoes.dto.JobResponseDTO;
import com.Projeto.GeradorDeQuestoes.entities.ExtracaoJobEntity;
import com.Projeto.GeradorDeQuestoes.repositories.PdfQuestaoRepository;
import com.Projeto.GeradorDeQuestoes.services.ExtracaoJobService;
import com.Projeto.GeradorDeQuestoes.services.IngestaoMaterialService;

@RestController
@RequestMapping("/api/admin/material")
@CrossOrigin(origins = "*") 
public class IngestaoController {

    private final IngestaoMaterialService ingestaoService;
    private final ExtracaoJobService jobService;

    public IngestaoController(IngestaoMaterialService ingestaoService, 
        ExtracaoJobService jobService, 
        PdfQuestaoRepository pdfQuestaoRepository) {
        this.ingestaoService = ingestaoService;
        this.jobService = jobService;
    }


    @PostMapping("/upload/dificil")
    public ResponseEntity<String> uploadMaterialDificil(
            @RequestParam("file") MultipartFile file,
            @RequestParam("topico") String topico,
            @RequestParam("fonte") String fonte) {
        
            try {

                    byte[] bytes = file.getBytes();
                    Resource pdfResource = new ByteArrayResource(bytes) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename(); 
                        }
                    };

                    ingestaoService.importarCapituloLivroDificil(pdfResource, topico, fonte);
                    
                    return ResponseEntity.ok("Material processado e indexado com sucesso no PGVector!");

                } catch (Exception e) {

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erro ao processar PDF: " + e.getMessage());

                }
    }

    @PostMapping("/upload/medio")
    public ResponseEntity<String> uploadMaterialMedio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("topico") String topico,
            @RequestParam("fonte") String fonte) {
        
        try {

            Resource pdfResource = file.getResource();
            
            ingestaoService.importarCapituloLivroMedio(pdfResource, topico, fonte);
            
            return ResponseEntity.ok("Material processado e indexado com sucesso no PGVector!");

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar PDF: " + e.getMessage());

        }
    }

    @PostMapping("/upload/facil")
    public ResponseEntity<String> uploadMaterialFacil(
            @RequestParam("file") MultipartFile file,
            @RequestParam("topico") String topico,
            @RequestParam("fonte") String fonte) {
        
        try {

            Resource pdfResource = file.getResource();
            
            ingestaoService.importarCapituloLivroFacil(pdfResource, topico, fonte);
            
            return ResponseEntity.ok("Material processado e indexado com sucesso no PGVector!");

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar PDF: " + e.getMessage());

        }
    }

   
    @PostMapping("/upload/questoes/async")
    public ResponseEntity<JobResponseDTO> extrairQuestoesDeProvaAsync(
            @RequestParam("file") MultipartFile file,
            @RequestParam("disciplinaId") String disciplinaId,
            @RequestParam(value = "prompt", required = false) String promptPersonalizado,
            @RequestParam("modoExtracao") String modoExtracao) { 
    
        File tempFile = null;
        try {
            String nomeOriginal = file.getOriginalFilename();
            if (nomeOriginal == null || nomeOriginal.trim().isEmpty()) {
                nomeOriginal = "documento_sem_nome.pdf";
            }

            String jobId = UUID.randomUUID().toString();

            tempFile = File.createTempFile("prova_async_" + jobId + "_", ".pdf");
            file.transferTo(tempFile);
            
            ExtracaoJobEntity novoJob = new ExtracaoJobEntity(jobId, "PENDING", nomeOriginal, "EXTRACAO_PROVA");
            novoJob.setDisciplinaId(disciplinaId); 
            novoJob.setModoExtracao(modoExtracao);
            novoJob.setCaminhoArquivoTemporario(tempFile.getAbsolutePath()); 
            jobService.salvar(novoJob);
            
            ingestaoService.enfileirarProcessamentoPdf(jobId, tempFile, disciplinaId, promptPersonalizado, modoExtracao);
            
            JobResponseDTO response = new JobResponseDTO(
                    jobId, "PENDING", "Processamento iniciado em segundo plano."
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            
        } catch (Exception e) {
            if (tempFile != null && tempFile.exists()) tempFile.delete();
            System.err.println("Erro no processamento async da prova: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}
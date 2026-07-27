package com.Projeto.GeradorDeQuestoes.controllers;

import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Projeto.GeradorDeQuestoes.entities.PdfBinarioEntity;
import com.Projeto.GeradorDeQuestoes.services.PdfBinarioService;

@RestController
@RequestMapping("/api/documentacao")
public class VectorIngestionController {

    private final PdfBinarioService pdfBinarioService; 

    public VectorIngestionController(
            PdfBinarioService pdfBinarioService) {
 
        this.pdfBinarioService = pdfBinarioService;
    }


    @GetMapping("/download/{idBinario}")
    public ResponseEntity<byte[]> downloadMaterial(@PathVariable UUID idBinario) {
        try {

            PdfBinarioEntity pdf = pdfBinarioService.buscarPorId(idBinario);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_PDF);
            
            headers.setContentDispositionFormData("attachment", pdf.getNomeOriginal());
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf.getArquivoBinario());

        } catch (IllegalArgumentException e) {

            return ResponseEntity.notFound().build();

        } catch (Exception e) {

            return ResponseEntity.internalServerError().build();
            
        }
    }


}
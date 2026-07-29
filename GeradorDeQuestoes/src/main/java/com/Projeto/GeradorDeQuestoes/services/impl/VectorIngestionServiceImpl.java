package com.Projeto.GeradorDeQuestoes.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.dto.ResultadoIngestaoDTO;
import com.Projeto.GeradorDeQuestoes.entities.BancoQuestaoEntity;
import com.Projeto.GeradorDeQuestoes.entities.ConceitoEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.services.ConceitoService;
import com.Projeto.GeradorDeQuestoes.services.GeradorQuestaoService;
import com.Projeto.GeradorDeQuestoes.services.VectorIngestionService;

@Service
public class VectorIngestionServiceImpl implements VectorIngestionService {

    private final VectorStore vectorStore;

    private final GeradorQuestaoService geradorQuestaoService;

    private final ConceitoService conceitoService;


    public VectorIngestionServiceImpl(VectorStore vectorStore, 
        GeradorQuestaoService geradorQuestaoService,
        ConceitoService conceitoService) {
        this.vectorStore = vectorStore;
        this.geradorQuestaoService = geradorQuestaoService;
        this.conceitoService = conceitoService;
    }





    @Override
    public ResultadoIngestaoDTO ingerirPdf(byte[] pdfBytes, String filename, Map<String, Object> metadata, UsuarioEntity usuario) {

        ByteArrayResource resource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() { return filename; }
        };

        PagePdfDocumentReader reader = new PagePdfDocumentReader(
            resource,
            PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageBottomMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                .withPagesPerDocument(1)
                .build()
        );

        List<Document> paginas = reader.get();
        
        List<Document> paginasValidas = paginas.stream()
            .filter(d -> d.getText() != null && !d.getText().isBlank())
            .toList();

        if (paginasValidas.isEmpty()) {
            System.out.println("⚠️ O leitor nativo não encontrou texto (possível PDF achatado). Acionando fallback com Apache Tika...");
            
            org.springframework.ai.reader.tika.TikaDocumentReader tikaReader = 
                new org.springframework.ai.reader.tika.TikaDocumentReader(resource);
            
            List<Document> tikaDocs = tikaReader.get();
            
            paginasValidas = tikaDocs.stream()
                .filter(d -> d.getText() != null && !d.getText().isBlank())
                .toList();
        }

        if (paginasValidas.isEmpty()) {
            throw new IllegalArgumentException(
                "Falha na extração. O arquivo é puramente uma imagem e requer processamento externo de OCR (Reconhecimento Óptico) antes do upload."
            );
        }

        int totalCaracteres = paginasValidas.stream().mapToInt(d -> d.getText().length()).sum();
        int mediaCaracteresPorPagina = totalCaracteres / paginasValidas.size();
        
        boolean isApresentacaoSlides = mediaCaracteresPorPagina < 800; 
        System.out.println("Densidade média de texto: " + mediaCaracteresPorPagina + " caracteres por divisão.");

        String textoCompleto = paginasValidas.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n"));

        List<String> conceitosGlobais = geradorQuestaoService.extrairConceitosUnicos(textoCompleto, 20, usuario);
        
        String tituloDocumento = (String) metadata.get("titulo_documento");
        UUID documentoId = UUID.fromString((String) metadata.get("documento_id"));

        for (String nomeConceito : conceitosGlobais) {
            conceitoService.processarConceitoQuestao(
                nomeConceito, tituloDocumento, ConceitoEntity.TipoOrigem.DOCUMENTO, documentoId
            );
        }

        List<Document> chunksParaVetorizar;
        
        if (isApresentacaoSlides) {
            System.out.println("📊 Formato de Slides detectado. Aplicando estratégia de Chunking por Página.");
            chunksParaVetorizar = processarComoSlides(paginasValidas, conceitosGlobais, metadata);
        } else {
            System.out.println("📄 Formato de Documento Padrão detectado. Aplicando TokenTextSplitter.");
            chunksParaVetorizar = processarComoDocumentoPadrao(paginasValidas, conceitosGlobais, metadata);
        }

        vectorStore.accept(chunksParaVetorizar);
        System.out.println("Chunks inseridos com sucesso no pgvector: " + chunksParaVetorizar.size());

        return new ResultadoIngestaoDTO(chunksParaVetorizar.size(), conceitosGlobais);
    }


    private List<Document> processarComoSlides(List<Document> paginasValidas, List<String> conceitosGlobais, Map<String, Object> metadata) {
        List<Document> chunksProcessados = new ArrayList<>();

        for (Document pagina : paginasValidas) {
            String textoPaginaLower = pagina.getText().toLowerCase();
            
            List<String> conceitosNesteSlide = conceitosGlobais.stream()
                .filter(conceito -> textoPaginaLower.contains(conceito.toLowerCase()))
                .toList();

            if (conceitosNesteSlide.isEmpty()) {
                pagina.getMetadata().putAll(metadata);
                chunksProcessados.add(pagina);
            } else {
                for (String conceito : conceitosNesteSlide) {
                    Map<String, Object> novoMetadata = new HashMap<>(pagina.getMetadata());
                    novoMetadata.putAll(metadata);
                    novoMetadata.put("conceito", conceito); 
                    
                    chunksProcessados.add(new Document(pagina.getText(), novoMetadata));
                }
            }
        }
        return chunksProcessados;
    }

    private List<Document> processarComoDocumentoPadrao(List<Document> paginasValidas, List<String> conceitosGlobais, Map<String, Object> metadata) {
        List<Document> chunksProcessados = new ArrayList<>();
        TokenTextSplitter splitter = new TokenTextSplitter(512, 128, 5, 5000, true);
        List<Document> chunksOriginais = splitter.apply(paginasValidas);
        
        chunksOriginais.forEach(chunk -> {
            String chunkTextLower = chunk.getText().toLowerCase();
            
            List<String> conceitosNesteChunk = conceitosGlobais.stream()
                .filter(conceito -> chunkTextLower.contains(conceito.toLowerCase()))
                .toList();

            if (conceitosNesteChunk.isEmpty()) {
                chunk.getMetadata().putAll(metadata);
                chunksProcessados.add(chunk);
            } else {
                for (String conceito : conceitosNesteChunk) {
                    Map<String, Object> novoMetadata = new HashMap<>(chunk.getMetadata());
                    novoMetadata.putAll(metadata);
                    novoMetadata.put("conceito", conceito); 
                    
                    chunksProcessados.add(new Document(chunk.getText(), novoMetadata));
                }
            }
        });
        
        return chunksProcessados;
    }



    @Override
    public void vetorizarQuestaoDeProva(BancoQuestaoEntity questao) {
        
        StringBuilder textoVetorizado = new StringBuilder();
        
        textoVetorizado.append("Enunciado: ").append(questao.getEnunciado()).append("\n\n");
        textoVetorizado.append("Alternativas:\n");

        if (questao.getAlternativas() != null) {
            questao.getAlternativas().forEach((letra, texto) -> {
                textoVetorizado.append(letra.toUpperCase()).append(") ").append(texto).append("\n");
            });
        }

        String resposta = questao.getRespostaCorreta() != null ? questao.getRespostaCorreta().toUpperCase() : "N/A";
        textoVetorizado.append("\nResposta Correta: ").append(resposta);

        if (questao.getComentarioTecnico() != null && !questao.getComentarioTecnico().isBlank()) {
            textoVetorizado.append("\n\nComentário Técnico da Banca: ").append(questao.getComentarioTecnico());
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tipo_vetor", "QUESTAO_ESTILO");
        metadata.put("questao_id", questao.getId() != null ? questao.getId().toString() : UUID.randomUUID().toString());
        metadata.put("conceito", questao.getConceito() != null ? questao.getConceito() : "Geral");
        metadata.put("nivel", questao.getNivel() != null ? questao.getNivel().name() : "NAO_INFORMADO");
        metadata.put("competencia", questao.getCompetencia() != null ? questao.getCompetencia() : "NAO_INFORMADO");
        metadata.put("topico", questao.getTopico() != null ? questao.getTopico() : "Geral");

        if (questao.getArquivoOrigem() != null && questao.getArquivoOrigem().getId() != null) {
            metadata.put("documento_id", questao.getArquivoOrigem().getId().toString());
        }

        Document document = new Document(textoVetorizado.toString(), metadata);

        vectorStore.accept(List.of(document));

        System.out.println("✅ Questão de prova vetorizada com sucesso. Contexto armazenado no PGVector.");
    }
    
}

package com.Projeto.GeradorDeQuestoes.dto;

import java.time.LocalDateTime;

public class PdfQuestaoDTO {
    
    private String id;
    private String nomeOriginal;
    private String contentType;
    private Long tamanhoBytes;
    private LocalDateTime dataUpload;
    private String promptId;

    public String getId() { return id; }
    public String getNomeOriginal() { return nomeOriginal; }
    public String getContentType() { return contentType; }
    public Long getTamanhoBytes() { return tamanhoBytes; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public String getPromptId() { return promptId; }

    public void setId(String id) { this.id = id; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setTamanhoBytes(Long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
    public void setPromptId(String promptId) { this.promptId = promptId; }
}
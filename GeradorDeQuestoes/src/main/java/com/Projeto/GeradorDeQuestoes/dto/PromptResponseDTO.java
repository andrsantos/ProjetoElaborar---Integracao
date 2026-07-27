package com.Projeto.GeradorDeQuestoes.dto;

public class PromptResponseDTO {
    
    private String id;
    private String documentoId; 
    private String nivel;
    private String instrucao;
    private boolean ativo;

    public PromptResponseDTO() {
    }

    public PromptResponseDTO(String documentoId, String nivel, String instrucao, boolean ativo) {
        this.documentoId = documentoId;
        this.nivel = nivel;
        this.instrucao = instrucao;
        this.ativo = ativo;
    }

    public PromptResponseDTO(String id, String documentoId, String nivel, String instrucao, boolean ativo) {
        this.id = id;
        this.documentoId = documentoId;
        this.nivel = nivel;
        this.instrucao = instrucao;
        this.ativo = ativo;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentoId() {
        return this.documentoId;
    }

    public void setDocumentoId(String documentoId) {
        this.documentoId = documentoId;
    }
     
    public String getNivel() {
        return this.nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getInstrucao() {
        return this.instrucao;
    }

    public void setInstrucao(String instrucao) {
        this.instrucao = instrucao;
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    public boolean getAtivo() {
        return this.ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
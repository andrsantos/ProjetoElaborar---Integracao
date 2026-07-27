package com.Projeto.GeradorDeQuestoes.dto;

public class PromptRequestDTO {

    private String documentoId; 
    private String nivel; 
    private String instrucao;
    private boolean ativo;    

    public PromptRequestDTO() {
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
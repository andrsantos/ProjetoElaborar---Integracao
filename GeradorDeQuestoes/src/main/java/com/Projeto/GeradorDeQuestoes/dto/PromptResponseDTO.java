package com.Projeto.GeradorDeQuestoes.dto;

public class PromptResponseDTO {
    
    private String id;
    private String nome; 
    private String nivel;
    private String instrucao;
    private boolean ativo;

    public PromptResponseDTO() {
    }

    public PromptResponseDTO(String nome, String nivel, String instrucao, boolean ativo) {
        this.nome = nome;
        this.nivel = nivel;
        this.instrucao = instrucao;
        this.ativo = ativo;
    }

    public PromptResponseDTO(String id, String nome, String nivel, String instrucao, boolean ativo) {
        this.id = id;
        this.nome = nome;
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

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
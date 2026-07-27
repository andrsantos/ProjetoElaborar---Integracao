package com.Projeto.GeradorDeQuestoes.dto;

public class DisciplinaDTO {

    private String nome;
    private String descricao;

    public DisciplinaDTO() {
    }

    public DisciplinaDTO(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
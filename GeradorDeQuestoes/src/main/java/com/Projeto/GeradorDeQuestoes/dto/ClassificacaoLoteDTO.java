package com.Projeto.GeradorDeQuestoes.dto;

public class ClassificacaoLoteDTO {

    private String questaoId;
    private String topicoEscolhido;

    public String getQuestaoId() {
        return this.questaoId;
    }

    public void setQuestaoId(String questaoId) {
        this.questaoId = questaoId;
    }

    public String getTopicoEscolhido() {
        return this.topicoEscolhido;
    }

    public void setTopicoEscolhido(String topicoEscolhido) {
        this.topicoEscolhido = topicoEscolhido;
    }

}

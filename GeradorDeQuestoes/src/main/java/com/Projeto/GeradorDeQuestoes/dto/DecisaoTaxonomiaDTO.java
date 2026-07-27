package com.Projeto.GeradorDeQuestoes.dto;

public class DecisaoTaxonomiaDTO {

    private String conceitoFinal;
    private boolean conceitoEhNovo;
    private String justificativa;


    public DecisaoTaxonomiaDTO() {
    }


    public DecisaoTaxonomiaDTO(String conceitoFinal, boolean conceitoEhNovo, String justificativa) {
        this.conceitoFinal = conceitoFinal;
        this.conceitoEhNovo = conceitoEhNovo;
        this.justificativa = justificativa;
    }



    public String getConceitoFinal() {
        return this.conceitoFinal;
    }

    public void setConceitoFinal(String conceitoFinal) {
        this.conceitoFinal = conceitoFinal;
    }

    public boolean isConceitoEhNovo() {
        return this.conceitoEhNovo;
    }

    public boolean getConceitoEhNovo() {
        return this.conceitoEhNovo;
    }

    public void setConceitoEhNovo(boolean conceitoEhNovo) {
        this.conceitoEhNovo = conceitoEhNovo;
    }

    public String getJustificativa() {
        return this.justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }


    
}

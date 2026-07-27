package com.Projeto.GeradorDeQuestoes.entities;

public class ParecerJuizEntity {

    private boolean aprovada;
    private String critica; 
    

    public ParecerJuizEntity(boolean aprovada, String critica) {
        this.aprovada = aprovada;
        this.critica = critica;
    }


    public ParecerJuizEntity() {
    }



    public boolean isAprovada() {
        return this.aprovada;
    }

    public boolean getAprovada() {
        return this.aprovada;
    }

    public void setAprovada(boolean aprovada) {
        this.aprovada = aprovada;
    }

    public String getCritica() {
        return this.critica;
    }

    public void setCritica(String critica) {
        this.critica = critica;
    }

}

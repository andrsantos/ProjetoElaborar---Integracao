package com.Projeto.GeradorDeQuestoes.entities;

public class ParecerAmbiguidadeEntity {

    private boolean containsAmbiguity;
    private String feedback;
    private String questaoRaw;
    private String sugestaoReforma;



    public ParecerAmbiguidadeEntity(boolean containsAmbiguity, String feedback, String questaoRaw, String sugestaoReforma) {
        this.containsAmbiguity = containsAmbiguity;
        this.feedback = feedback;
        this.questaoRaw = questaoRaw;
        this.sugestaoReforma = sugestaoReforma;
    }
   
 

    public ParecerAmbiguidadeEntity() {
    }


    public boolean isContainsAmbiguity() {
        return this.containsAmbiguity;
    }

    public boolean getContainsAmbiguity() {
        return this.containsAmbiguity;
    }

    public void setContainsAmbiguity(boolean containsAmbiguity) {
        this.containsAmbiguity = containsAmbiguity;
    }

    public String getFeedback() {
        return this.feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getQuestaoRaw() {
        return this.questaoRaw;
    }

    public void setQuestaoRaw(String questaoRaw) {
        this.questaoRaw = questaoRaw;
    }

    public String getSugestaoReforma() {
        return this.sugestaoReforma;
    }

    public void setSugestaoReforma(String sugestaoReforma) {
        this.sugestaoReforma = sugestaoReforma;
    }

    
}

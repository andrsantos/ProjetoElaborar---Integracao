package com.Projeto.GeradorDeQuestoes.dto;

import java.util.List;

public class LoteQuestoesDTO {
    
    private String jobId;
    private List<QuestaoComOrigemDTO> questoes;

    public LoteQuestoesDTO() {}

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public List<QuestaoComOrigemDTO> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<QuestaoComOrigemDTO> questoes) {
        this.questoes = questoes;
    }
    
}
package com.Projeto.GeradorDeQuestoes.dto;

public class JobResponseDTO {
    private String jobId;
    private String status;
    private String mensagem;

    public JobResponseDTO(String jobId, String status, String mensagem) {
        this.jobId = jobId;
        this.status = status;
        this.mensagem = mensagem;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
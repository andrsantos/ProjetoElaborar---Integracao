package com.Projeto.GeradorDeQuestoes.dto;

import java.time.LocalDateTime;

public class JobResumoDTO {

    private String id;
    private String status;
    private String nomeOriginal;
    private String modoExtracao;
    private String tipo;
    private String mensagemErro;
    private LocalDateTime dataCriacao; 
    
    private boolean visualizado; 

    public JobResumoDTO() {
    }

    public JobResumoDTO(String id, String status, String nomeOriginal, String modoExtracao, String mensagemErro, String tipo, 
        LocalDateTime dataCriacao, boolean visualizado) {
        this.id = id;
        this.status = status;
        this.nomeOriginal = nomeOriginal;
        this.modoExtracao = modoExtracao;
        this.mensagemErro = mensagemErro;
        this.tipo = tipo;
        this.dataCriacao = dataCriacao;
        this.visualizado = visualizado;
    }

    public String getId() { return id; }
    public String getStatus() { return status; }
    public String getNomeOriginal() { return nomeOriginal; }
    public String getModoExtracao() { return modoExtracao; }
    public String getMensagemErro() { return mensagemErro; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public String getTipo() { return this.tipo; }
    
    public boolean isVisualizado() { return visualizado; } 

    public void setId(String id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }
    public void setModoExtracao(String modoExtracao) { this.modoExtracao = modoExtracao; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public void setVisualizado(boolean visualizado) { this.visualizado = visualizado; } 

}
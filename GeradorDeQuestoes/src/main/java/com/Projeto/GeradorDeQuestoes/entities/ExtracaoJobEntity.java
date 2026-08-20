package com.Projeto.GeradorDeQuestoes.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime; 

@Entity
@Table(name = "extracao_jobs")
public class ExtracaoJobEntity {

    @Id
    private String id; 

    private String status;

    private String nomeArquivo; 

    private String modoExtracao;

    @Column(name = "tipo_job", length = 50)
    private String tipo;

    private LocalDateTime dataCriacao;

    @Column(columnDefinition = "TEXT")
    private String resultadoJson; 

    @Column(columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "disciplina_id")
    private String disciplinaId;

    @Column(name = "caminho_arquivo_temporario")
    private String caminhoArquivoTemporario;

    @Column(name = "visualizado", nullable = false)
    private boolean visualizado = false;

    public ExtracaoJobEntity() {
        this.dataCriacao = LocalDateTime.now();
    }

    public ExtracaoJobEntity(String id, String status, String nomeArquivo, String tipo) {
        this.id = id;
        this.status = status;
        this.nomeArquivo = nomeArquivo;
        this.tipo = tipo;
        this.dataCriacao = LocalDateTime.now();
    }



    public ExtracaoJobEntity(String id, String status, String nomeArquivo) {
        this.id = id;
        this.status = status;
        this.nomeArquivo = nomeArquivo;
        this.dataCriacao = LocalDateTime.now();
    }

    public ExtracaoJobEntity(String id, String status) {
        this.id = id;
        this.status = status;
        this.tipo = "EXTRACAO_PROVA"; 
        this.dataCriacao = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getResultadoJson() { return resultadoJson; }
    public void setResultadoJson(String resultadoJson) { this.resultadoJson = resultadoJson; }
    public String getMensagemErro() { return mensagemErro; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
 
    public String getDisciplinaId() {
        return this.disciplinaId;
    }

    public void setDisciplinaId(String disciplinaId) {
        this.disciplinaId = disciplinaId;
    }


    public String getModoExtracao() {
        return this.modoExtracao;
    }

    public void setModoExtracao(String modoExtracao) {
        this.modoExtracao = modoExtracao;
    }

    public String getCaminhoArquivoTemporario() {
        return caminhoArquivoTemporario;
    }

    public void setCaminhoArquivoTemporario(String caminhoArquivoTemporario) {
        this.caminhoArquivoTemporario = caminhoArquivoTemporario;
    }


    public boolean isVisualizado() {
        return this.visualizado;
    }

    public boolean getVisualizado() {
        return this.visualizado;
    }

    public void setVisualizado(boolean visualizado) {
        this.visualizado = visualizado;
    }


}
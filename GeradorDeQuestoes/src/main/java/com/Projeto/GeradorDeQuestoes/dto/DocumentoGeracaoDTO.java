package com.Projeto.GeradorDeQuestoes.dto;

import java.util.List;

public class DocumentoGeracaoDTO {

    private String documentoId; 
    private List<ConceitoConfigDTO> subtopicos;
    private int quantidade;
    private int quantidadeDificeis;
    private int quantidadeMedias;
    private int quantidadeFaceis;
    private String diretrizCustomizada;

    public String getDocumentoId() { return this.documentoId; }
    public void setDocumentoId(String documentoId) { this.documentoId = documentoId; }

    public List<ConceitoConfigDTO> getSubtopicos() { return this.subtopicos; }
    public void setSubtopicos(List<ConceitoConfigDTO> subtopicos) { this.subtopicos = subtopicos; }



    public int getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public int getQuantidadeDificeis() {
        return this.quantidadeDificeis;
    }

    public void setQuantidadeDificeis(int quantidadeDificeis) {
        this.quantidadeDificeis = quantidadeDificeis;
    }

    public int getQuantidadeMedias() {
        return this.quantidadeMedias;
    }

    public void setQuantidadeMedias(int quantidadeMedias) {
        this.quantidadeMedias = quantidadeMedias;
    }

    public int getQuantidadeFaceis() {
        return this.quantidadeFaceis;
    }

    public void setQuantidadeFaceis(int quantidadeFaceis) {
        this.quantidadeFaceis = quantidadeFaceis;
    }

    public String getDiretrizCustomizada() {
        return this.diretrizCustomizada;
    }

    public void setDiretrizCustomizada(String diretrizCustomizada) {
        this.diretrizCustomizada = diretrizCustomizada;
    }


    
}

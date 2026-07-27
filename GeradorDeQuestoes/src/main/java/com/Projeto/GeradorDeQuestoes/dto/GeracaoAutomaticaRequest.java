package com.Projeto.GeradorDeQuestoes.dto;

import java.util.List;
public class GeracaoAutomaticaRequest{


    private List<DocumentoGeracaoDTO> documentos; 

    public List<DocumentoGeracaoDTO> getDocumentos() { return this.documentos; }
    public void setDocumentos(List<DocumentoGeracaoDTO> documentos) { this.documentos = documentos; }



}
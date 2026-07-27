package com.Projeto.GeradorDeQuestoes.dto;

import java.util.List;

public class GeracaoExpressaRequest {
    
    private String disciplinaId;
    private Integer quantidade;
    private String nivel; 
    private List<String> topicos;
    private String diretriz; 

    public GeracaoExpressaRequest() {}
    

    public String getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(String disciplinaId) { this.disciplinaId = disciplinaId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public List<String> getTopicos() { return topicos; }
    public void setTopicos(List<String> topicos) { this.topicos = topicos; }

    public String getDiretriz() { return diretriz; }
    public void setDiretriz(String diretriz) { this.diretriz = diretriz; }

    
}

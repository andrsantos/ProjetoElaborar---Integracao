package com.Projeto.GeradorDeQuestoes.dto;

import java.util.List;

public class TaxonomiaDTO {
    
    private String nomeDisciplina;
    private List<String> palavrasChave;
    private List<String> topicos;

    public TaxonomiaDTO() {}

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    public List<String> getPalavrasChave() { return palavrasChave; }
    public void setPalavrasChave(List<String> palavrasChave) { this.palavrasChave = palavrasChave; }

    public List<String> getTopicos() { return topicos; }
    public void setTopicos(List<String> topicos) { this.topicos = topicos; }

}

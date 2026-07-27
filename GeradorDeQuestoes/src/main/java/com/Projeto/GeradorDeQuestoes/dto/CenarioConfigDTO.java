package com.Projeto.GeradorDeQuestoes.dto;

public class CenarioConfigDTO {

    private Long id;
    private String topico;
    private String nivel;
    private String descricao;

    public CenarioConfigDTO() {
    }

    public CenarioConfigDTO(String topico, String nivel, String descricao) {
        this.topico = topico;
        this.nivel = nivel;
        this.descricao = descricao;
    }
    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopico() {
        return this.topico;
    }

    public void setTopico(String topico) {
        this.topico = topico;
    }

    public String getNivel() {
        return this.nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    
}

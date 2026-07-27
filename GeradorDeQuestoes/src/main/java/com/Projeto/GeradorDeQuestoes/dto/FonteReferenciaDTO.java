package com.Projeto.GeradorDeQuestoes.dto;

public class FonteReferenciaDTO {
    
    private String id;
    private String titulo;
    private String tipo; 

    public FonteReferenciaDTO() {}

    public FonteReferenciaDTO(String id, String titulo, String tipo) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
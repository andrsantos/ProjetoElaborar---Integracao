package com.Projeto.GeradorDeQuestoes.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_documentos_referencia")
public class DocumentosReferenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String titulo; 

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "pdf_binario_id", referencedColumnName = "id")
    private PdfBinarioEntity pdfBinario; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disciplina_id", nullable = false)
    private DisciplinaEntity disciplina;

    public DocumentosReferenciaEntity() {}

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public PdfBinarioEntity getPdfBinario() {
        return pdfBinario;
    }

    public void setPdfBinario(PdfBinarioEntity pdfBinario) {
        this.pdfBinario = pdfBinario;
    }

    public DisciplinaEntity getDisciplina() {
        return this.disciplina;
    }

    public void setDisciplina(DisciplinaEntity disciplina) {
        this.disciplina = disciplina;
    }
}
package com.Projeto.GeradorDeQuestoes.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "tb_template_topico")
public class TemplateTopicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_disciplina_id", nullable = false)
    private TemplateDisciplinaEntity templateDisciplina;

    public TemplateTopicoEntity() {
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TemplateDisciplinaEntity getTemplateDisciplina() {
        return templateDisciplina;
    }

    public void setTemplateDisciplina(TemplateDisciplinaEntity templateDisciplina) {
        this.templateDisciplina = templateDisciplina;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateTopicoEntity that = (TemplateTopicoEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


    @Override
    public String toString() {
        return "TemplateTopicoEntity{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                '}';
    }
}
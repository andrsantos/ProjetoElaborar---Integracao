package com.Projeto.GeradorDeQuestoes.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_template_disciplina")
public class TemplateDisciplinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nomeDisciplina;

    @ElementCollection
    @CollectionTable(
            name = "tb_template_palavras_chave", 
            joinColumns = @JoinColumn(name = "template_id")
    )
    @Column(name = "palavra")
    private List<String> palavrasChave = new ArrayList<>();

    @OneToMany(mappedBy = "templateDisciplina", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemplateTopicoEntity> topicos = new ArrayList<>();

    public TemplateDisciplinaEntity() {
    }

    public void addTopico(TemplateTopicoEntity topico) {
        topicos.add(topico);
        topico.setTemplateDisciplina(this);
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public List<String> getPalavrasChave() {
        return palavrasChave;
    }

    public void setPalavrasChave(List<String> palavrasChave) {
        this.palavrasChave = palavrasChave;
    }

    public List<TemplateTopicoEntity> getTopicos() {
        return topicos;
    }

    public void setTopicos(List<TemplateTopicoEntity> topicos) {
        this.topicos = topicos;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateDisciplinaEntity that = (TemplateDisciplinaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


    @Override
    public String toString() {
        return "TemplateDisciplinaEntity{" +
                "id=" + id +
                ", nomeDisciplina='" + nomeDisciplina + '\'' +
                ", palavrasChave=" + palavrasChave +
                '}';
    }
}

package com.Projeto.GeradorDeQuestoes.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_prompts")
public class PromptEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "nome", nullable = false, unique = true)
    private String nome;

    @Column(name = "nivel")
    private String nivel; 

    @Column(name = "instrucao", columnDefinition = "TEXT", nullable = false)
    private String instrucao;

    @Column(name = "ativo")
    private boolean ativo = true;

    @Column(name = "disciplina_id", nullable = true)
    private String disciplinaId;

    @Column(name = "is_padrao", nullable = false)
    private boolean isPadrao = false;



    public PromptEntity() {}

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getNivel() { return this.nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getInstrucao() { return this.instrucao; }
    public void setInstrucao(String instrucao) { this.instrucao = instrucao; }

    public boolean isAtivo() { return this.ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }


    public boolean getAtivo() {
        return this.ativo;
    }


    public String getDisciplinaId() {
        return this.disciplinaId;
    }

    public void setDisciplinaId(String disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public boolean isIsPadrao() {
        return this.isPadrao;
    }

    public boolean getIsPadrao() {
        return this.isPadrao;
    }

    public void setIsPadrao(boolean isPadrao) {
        this.isPadrao = isPadrao;
    }

}
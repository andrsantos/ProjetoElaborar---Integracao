package com.Projeto.GeradorDeQuestoes.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Prova {

    private final UUID id;
    private final List<Questao> questoes;
    private String disciplinaId; 

    public Prova() {
        this.id = UUID.randomUUID(); 
        this.questoes = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }

    public String getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(String disciplinaId) {
        this.disciplinaId = disciplinaId;
    }

    public void adicionarQuestao(Questao questao) {
        this.questoes.add(questao);
    }

    public void removerQuestao(int indice) {
        if (indice >= 0 && indice < this.questoes.size()) {
            this.questoes.remove(indice);
        }
    }
}
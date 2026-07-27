package com.Projeto.GeradorDeQuestoes.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "topico_config")
public class TopicoConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; 

    @Column(name = "topico")
    private String topico;

    @Column(name = "nivel")
    private String nivel;

    @Column(name = "instrucoes_especificas", columnDefinition = "TEXT")
    private String instrucoesEspecificas;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    


    public LocalDateTime getDataAtualizacao() {
        return this.dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getNivel() {
        return this.nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }


    public String getTopico() {
        return this.topico;
    }

    public void setTopico(String topico) {
        this.topico = topico;
    }

    public String getInstrucoesEspecificas() {
        return this.instrucoesEspecificas;
    }

    public void setInstrucoesEspecificas(String instrucoesEspecificas) {
        this.instrucoesEspecificas = instrucoesEspecificas;
    }

}
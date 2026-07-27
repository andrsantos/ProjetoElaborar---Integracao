package com.Projeto.GeradorDeQuestoes.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_conceitos")
public class ConceitoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 100)
    private String disciplina;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origem", nullable = false, length = 20)
    private TipoOrigem tipoOrigem;

    @Column(name = "origem_id")
    private UUID origemId;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public ConceitoEntity() {
    }

    public enum TipoOrigem {
        DOCUMENTO,
        PROVA
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDisciplina() {
        return this.disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public TipoOrigem getTipoOrigem() {
        return this.tipoOrigem;
    }

    public void setTipoOrigem(TipoOrigem tipoOrigem) {
        this.tipoOrigem = tipoOrigem;
    }

    public UUID getOrigemId() {
        return this.origemId;
    }

    public void setOrigemId(UUID origemId) {
        this.origemId = origemId;
    }

    public LocalDateTime getCriadoEm() {
        return this.criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return this.atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

}
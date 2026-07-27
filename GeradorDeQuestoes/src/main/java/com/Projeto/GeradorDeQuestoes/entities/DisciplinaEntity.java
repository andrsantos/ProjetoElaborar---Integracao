package com.Projeto.GeradorDeQuestoes.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_disciplinas")
public class DisciplinaEntity {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private UsuarioEntity usuario;

    public DisciplinaEntity() {
        this.id = UUID.randomUUID().toString();
    }

    public DisciplinaEntity(String nome, String descricao, UsuarioEntity usuario) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.descricao = descricao;
        this.usuario = usuario;
    }


    public String getId() { return id; }
    
    public void setId(String id) { this.id = id; }
    
    public String getNome() { return nome; }
    
    public void setNome(String nome) { this.nome = nome; }
    
    public String getDescricao() { return descricao; }

    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public LocalDateTime getDataCriacao() { return dataCriacao; }

    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    
    @JsonIgnore
    public UsuarioEntity getUsuario() { return usuario; }

    public void setUsuario(UsuarioEntity usuario) { this.usuario = usuario; }
}
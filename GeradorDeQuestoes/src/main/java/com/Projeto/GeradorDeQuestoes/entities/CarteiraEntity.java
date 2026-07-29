package com.Projeto.GeradorDeQuestoes.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "tb_carteiras")
public class CarteiraEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private UsuarioEntity usuario; 

    @Column(precision = 19, scale = 6, nullable = false)
    private BigDecimal saldoAtual;

    @Column(name = "data_proxima_recarga", nullable = false)
    private LocalDate dataProximaRecarga;

    @Version
    private Long versao;



    public CarteiraEntity() {
    }


    public CarteiraEntity(Long id, UsuarioEntity usuario, BigDecimal saldoAtual, LocalDate dataProximaRecarga, Long versao) {
        this.id = id;
        this.usuario = usuario;
        this.saldoAtual = saldoAtual;
        this.dataProximaRecarga = dataProximaRecarga;
        this.versao = versao;
    }



    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UsuarioEntity getUsuario() {
        return this.usuario;
    }

    public void setUsuario(UsuarioEntity usuario) {
        this.usuario = usuario;
    }

    public BigDecimal getSaldoAtual() {
        return this.saldoAtual;
    }

    public void setSaldoAtual(BigDecimal saldoAtual) {
        this.saldoAtual = saldoAtual;
    }

    public LocalDate getDataProximaRecarga() {
        return this.dataProximaRecarga;
    }

    public void setDataProximaRecarga(LocalDate dataProximaRecarga) {
        this.dataProximaRecarga = dataProximaRecarga;
    }

    public Long getVersao() {
        return this.versao;
    }

    public void setVersao(Long versao) {
        this.versao = versao;
    }



}

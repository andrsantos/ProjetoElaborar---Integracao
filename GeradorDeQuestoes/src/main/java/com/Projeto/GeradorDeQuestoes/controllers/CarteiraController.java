package com.Projeto.GeradorDeQuestoes.controllers;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.services.CarteiraService;

@RestController
@RequestMapping("/api/carteira")
public class CarteiraController {

    private final CarteiraService carteiraService;

    public CarteiraController(CarteiraService carteiraService) {
        this.carteiraService = carteiraService;
    }

    @GetMapping("/saldo")
    public ResponseEntity<BigDecimal> getSaldo(@AuthenticationPrincipal UsuarioEntity usuarioLogado) {
        BigDecimal saldo = carteiraService.consultarSaldoAtual(usuarioLogado);
        System.out.println("Saldo atual: " + saldo);
        return ResponseEntity.ok(saldo);
    }
}
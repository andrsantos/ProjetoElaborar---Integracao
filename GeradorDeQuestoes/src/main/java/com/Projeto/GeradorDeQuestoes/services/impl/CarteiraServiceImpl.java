package com.Projeto.GeradorDeQuestoes.services.impl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.entities.CarteiraEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.repositories.CarteiraRepository;
import com.Projeto.GeradorDeQuestoes.services.CarteiraService;

@Service
public class CarteiraServiceImpl implements CarteiraService {

    private final CarteiraRepository carteiraRepository;

    public CarteiraServiceImpl(CarteiraRepository carteiraRepository) {
        this.carteiraRepository = carteiraRepository;
    }

    @Override
    public CarteiraEntity inicializarCarteiraFreemium(UsuarioEntity usuario) {
        CarteiraEntity carteira = new CarteiraEntity();
        carteira.setUsuario(usuario);
        
        carteira.setSaldoAtual(new BigDecimal("2.00")); 
        
        carteira.setDataProximaRecarga(LocalDate.now().plusDays(30)); 
        
        return carteiraRepository.save(carteira);
    }

    @Override
    public BigDecimal consultarSaldoAtual(UsuarioEntity usuario) {
        CarteiraEntity carteira = carteiraRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada para o usuário solicitado."));
        
        return carteira.getSaldoAtual();
    }
    
}
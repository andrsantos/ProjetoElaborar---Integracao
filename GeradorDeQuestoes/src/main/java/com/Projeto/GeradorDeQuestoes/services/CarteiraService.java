package com.Projeto.GeradorDeQuestoes.services;

import java.math.BigDecimal;

import com.Projeto.GeradorDeQuestoes.entities.CarteiraEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

public interface CarteiraService {

    CarteiraEntity inicializarCarteiraFreemium(UsuarioEntity usuario);
    BigDecimal consultarSaldoAtual(UsuarioEntity usuario);
    
}

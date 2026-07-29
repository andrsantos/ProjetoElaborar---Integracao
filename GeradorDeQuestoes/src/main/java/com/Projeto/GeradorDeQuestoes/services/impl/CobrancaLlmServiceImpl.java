package com.Projeto.GeradorDeQuestoes.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.Projeto.GeradorDeQuestoes.entities.CarteiraEntity;
import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import com.Projeto.GeradorDeQuestoes.exceptions.SaldoInsuficienteException;
import com.Projeto.GeradorDeQuestoes.repositories.CarteiraRepository;
import com.Projeto.GeradorDeQuestoes.services.CobrancaLlmService;

import jakarta.transaction.Transactional;

@Service
public class CobrancaLlmServiceImpl implements CobrancaLlmService {

    private final CarteiraRepository carteiraRepository;


    // PREÇOS ATUALIZADOS - Cotação exata: Dólar a R$ 5,14
    
    // GPT-4o: US$ 2.50 / 1M input e US$ 10.00 / 1M output
    // Input: 2.50 * 5.14 / 1.000.000 = 0.00001285
    private static final BigDecimal GPT4O_INPUT = new BigDecimal("0.00001285");
    // Output: 10.00 * 5.14 / 1.000.000 = 0.0000514
    private static final BigDecimal GPT4O_OUTPUT = new BigDecimal("0.0000514");

    // Claude Haiku: US$ 1.00 / 1M input e US$ 5.00 / 1M output
    // Input: 1.00 * 5.14 / 1.000.000 = 0.00000514
    private static final BigDecimal HAIKU_INPUT = new BigDecimal("0.00000514");
    // Output: 5.00 * 5.14 / 1.000.000 = 0.0000257
    private static final BigDecimal HAIKU_OUTPUT = new BigDecimal("0.0000257");

    public CobrancaLlmServiceImpl(CarteiraRepository carteiraRepository) {
        this.carteiraRepository = carteiraRepository;
    }

 
    @Override
    @Transactional
    public void verificarSaldoMinimo(UsuarioEntity usuario) {
        CarteiraEntity carteira = buscarERenovarCarteira(usuario);

        if (carteira.getSaldoAtual().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente. Aguarde a renovação da sua cota ou adicione créditos.");
        }
    }

    @Override
    @Transactional
    public void deduzirCusto(UsuarioEntity usuario, long inputTokens, long outputTokens, String modelo) {
        CarteiraEntity carteira = buscarERenovarCarteira(usuario);

        BigDecimal custo = calcularCusto(inputTokens, outputTokens, modelo);
        
        carteira.setSaldoAtual(carteira.getSaldoAtual().subtract(custo));
        carteiraRepository.save(carteira);

        if (carteira.getSaldoAtual().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente. A operação foi interrompida.");
        }
    }

    private CarteiraEntity buscarERenovarCarteira(UsuarioEntity usuario) {
        CarteiraEntity carteira = carteiraRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Carteira não encontrada para o usuário: " + usuario.getEmail()));

        if (LocalDate.now().isAfter(carteira.getDataProximaRecarga()) || 
            LocalDate.now().isEqual(carteira.getDataProximaRecarga())) {
            
            carteira.setSaldoAtual(new BigDecimal("2.00")); 
            carteira.setDataProximaRecarga(LocalDate.now().plusDays(30));
            carteira = carteiraRepository.save(carteira);
        }

        return carteira;
    }

    private BigDecimal calcularCusto(long inputTokens, long outputTokens, String modelo) {
        BigDecimal precoInput;
        BigDecimal precoOutput;

        if (modelo != null && modelo.toLowerCase().contains("gpt-4o")) {
            precoInput = GPT4O_INPUT;
            precoOutput = GPT4O_OUTPUT;
        } else if (modelo != null && modelo.toLowerCase().contains("haiku")) {
            precoInput = HAIKU_INPUT;
            precoOutput = HAIKU_OUTPUT;
        } else {
            precoInput = GPT4O_INPUT;
            precoOutput = GPT4O_OUTPUT;
        }

        BigDecimal custoInput = precoInput.multiply(new BigDecimal(inputTokens));
        BigDecimal custoOutput = precoOutput.multiply(new BigDecimal(outputTokens));

        return custoInput.add(custoOutput).setScale(6, RoundingMode.HALF_UP);
    }
    
}

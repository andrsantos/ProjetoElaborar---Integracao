package com.Projeto.GeradorDeQuestoes.services;

import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class TokenService {

    @Value("${api.security.token.secret:uma-chave-secreta-muito-grande-para-o-gerador-de-questoes-jwt-256-bits}")
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String gerarToken(UsuarioEntity usuario) {
        long tempoExpiracao = 1000 * 60 * 60 * 2; 

        return Jwts.builder()
                .setIssuer("API Elaborar")
                .setSubject(usuario.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tempoExpiracao))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String validarTokenPegarEmail(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); 
        } catch (Exception e) {
            return null; 
        }
    }
}
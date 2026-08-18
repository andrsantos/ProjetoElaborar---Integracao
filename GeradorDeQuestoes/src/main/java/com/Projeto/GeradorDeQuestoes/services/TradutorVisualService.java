package com.Projeto.GeradorDeQuestoes.services;

import com.Projeto.GeradorDeQuestoes.entities.UsuarioEntity;

public interface TradutorVisualService {

    String traduzirImagemComContexto(String base64Image, String textoDaPagina, UsuarioEntity usuario);
    
}

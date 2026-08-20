package com.Projeto.GeradorDeQuestoes.services;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseNotificationService {

    SseEmitter subscribe(String disciplinaId);
    void notificarAtualizacao(String disciplinaId);
    
}

package com.Projeto.GeradorDeQuestoes.services.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.Projeto.GeradorDeQuestoes.services.SseNotificationService;

@Service
public class SseNotificationServiceImpl implements SseNotificationService {


    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String disciplinaId) {
        SseEmitter emitter = new SseEmitter(1800000L);
        
        emitters.computeIfAbsent(disciplinaId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(disciplinaId, emitter));
        emitter.onTimeout(() -> removeEmitter(disciplinaId, emitter));
        emitter.onError((e) -> removeEmitter(disciplinaId, emitter));

        return emitter;
    }



    public void notificarAtualizacao(String disciplinaId) {
        List<SseEmitter> disciplinaEmitters = emitters.get(disciplinaId);
        if (disciplinaEmitters != null) {
            for (SseEmitter emitter : disciplinaEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("job-update").data("ATUALIZAR"));
                } catch (IOException e) {
                    emitter.complete();
                    removeEmitter(disciplinaId, emitter);
                }
            }
        }
    }

    private void removeEmitter(String disciplinaId, SseEmitter emitter) {
        List<SseEmitter> disciplinaEmitters = emitters.get(disciplinaId);
        if (disciplinaEmitters != null) {
            disciplinaEmitters.remove(emitter);
            if (disciplinaEmitters.isEmpty()) {
                emitters.remove(disciplinaId);
            }
        }
    }
    
    
}

package com.Projeto.GeradorDeQuestoes.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProvaInfoDTO(
    UUID id,
    String titulo,
    OffsetDateTime dataCriacao,
    long numeroQuestoes 
) {
}
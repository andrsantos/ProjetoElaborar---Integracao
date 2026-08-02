package com.Projeto.GeradorDeQuestoes.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PdfQuestaoResumoDTO {
    UUID getId();
    String getNomeOriginal();
    Long getTamanhoBytes();
    LocalDateTime getDataUpload();
    Long getQuantidadeQuestoes();
}

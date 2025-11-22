package com.ceara_sem_fome_back.dto;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        String mensagem,
        LocalDateTime data,
        boolean lida
) {}
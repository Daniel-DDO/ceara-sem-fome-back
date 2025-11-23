package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        String mensagem,
        LocalDateTime data,
        boolean lida
) {
    public NotificacaoResponseDTO(Notificacao notificacao) {
        this(
                notificacao.getId(),
                notificacao.getMensagem(),
                notificacao.getDataCriacao(),
                notificacao.isLida()
        );
    }
}
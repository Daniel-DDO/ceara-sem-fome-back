package com.ceara_sem_fome_back.config;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificacaoEvent extends ApplicationEvent {
    private final String destinatarioId;
    private final String mensagem;

    public NotificacaoEvent(Object source, String destinatarioId, String mensagem) {
        super(source);
        this.destinatarioId = destinatarioId;
        this.mensagem = mensagem;
    }
}
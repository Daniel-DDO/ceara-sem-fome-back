package com.ceara_sem_fome_back.config;

import com.ceara_sem_fome_back.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificacaoListener {

    private final NotificacaoService notificacaoService;

    @Async
    @EventListener
    public void handleNotificacaoEvent(NotificacaoEvent event) {
        notificacaoService.criarEEnviarNotificacao(event.getDestinatarioId(), event.getMensagem());
    }
}
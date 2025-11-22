package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.NotificacaoResponseDTO;
import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacaoService {

    private final NotificacaoRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void criarEEnviarNotificacao(String destinatarioId, String mensagem) {
        Notificacao notificacao = new Notificacao(destinatarioId, mensagem);
        notificacao = repository.save(notificacao);

        NotificacaoResponseDTO dto = new NotificacaoResponseDTO(
                notificacao.getId(),
                notificacao.getMensagem(),
                notificacao.getDataCriacao(),
                notificacao.isLida()
        );

        String destino = "/topic/usuario/" + destinatarioId;
        messagingTemplate.convertAndSend(destino, dto);

        log.info("Notificação enviada para usuário {} via WebSocket", destinatarioId);
    }

    public List<Notificacao> listarPorUsuario(String usuarioId) {
        return repository.findByDestinatarioIdOrderByDataCriacaoDesc(usuarioId);
    }

    @Transactional
    public void marcarComoLida(Long notificacaoId) {
        repository.findById(notificacaoId).ifPresent(n -> {
            n.setLida(true);
            repository.save(n);
        });
    }
}
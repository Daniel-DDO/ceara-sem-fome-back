package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.model.Pessoa;
import com.ceara_sem_fome_back.repository.NotificacaoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    /**
     * Cria uma nova notificação para um destinatário específico.
     *
     * @param remetenteId O ID do usuário (Pessoa) que envia a notificação.
     * @param destinatarioId O ID do usuário (Pessoa) que receberá a notificação.
     * @param mensagem O conteúdo da notificação.
     * @return A notificação criada e salva.
     */
    public Notificacao criarNotificacao(String remetenteId, String destinatarioId, String mensagem) {
        Notificacao novaNotificacao = new Notificacao(remetenteId, destinatarioId, mensagem);
        log.info("Enviando notificação de {} para {}: {}", remetenteId, destinatarioId, mensagem);
        return notificacaoRepository.save(novaNotificacao);
    }

    /**
     * Busca todas as notificações para um usuário específico.
     *
     * @param destinatarioId O ID do usuário (Pessoa) cujas notificações serão buscadas.
     * @return Uma lista de notificações.
     */
    public List<Notificacao> buscarNotificacoesPorPessoa(String destinatarioId) {
        return notificacaoRepository.findByDestinatarioIdOrderByDataCriacaoDesc(destinatarioId);
    }
}

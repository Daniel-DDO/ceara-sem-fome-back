package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.model.Pessoa;
import com.ceara_sem_fome_back.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    /**
     * Cria uma nova notificação para um destinatário específico.
     *
     * @param destinatarioId O ID do usuário (Pessoa) que receberá a notificação.
     * @param mensagem O conteúdo da notificação.
     * @return A notificação criada e salva.
     */
    public Notificacao criarNotificacao(String destinatarioId, String mensagem) {
        // A validação se o destinatarioId existe pode ser feita aqui ou no serviço que chama este método.
        Notificacao novaNotificacao = new Notificacao(destinatarioId, mensagem);
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

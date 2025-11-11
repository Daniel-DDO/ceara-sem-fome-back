
package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.model.Pessoa;
import com.ceara_sem_fome_back.repository.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    /**
     * Cria uma nova notificação para um usuário.
     * @param destinatario A pessoa que receberá a notificação.
     * @param mensagem O conteúdo da notificação.
     * @param link O link de destino ao clicar na notificação (opcional).
     */
    @Transactional
    public void criarNotificacao(Pessoa destinatario, String mensagem, String link) {
        Notificacao notificacao = Notificacao.builder()
                .destinatario(destinatario)
                .mensagem(mensagem)
                .link(link)
                .lida(false)
                .build();
        notificacaoRepository.save(notificacao);
    }

    /**
     * Retorna a contagem de notificações não lidas para um usuário.
     * @param usuario O usuário para verificar.
     * @return O número de notificações não lidas.
     */
    @Transactional(readOnly = true)
    public long getContagemNaoLidas(Pessoa usuario) {
        return notificacaoRepository.countByDestinatarioAndLidaIsFalse(usuario);
    }

    /**
     * Retorna todas as notificações de um usuário.
     * @param usuario O usuário para buscar as notificações.
     * @return A lista de notificações.
     */
    @Transactional(readOnly = true)
    public List<Notificacao> getNotificacoes(Pessoa usuario) {
        return notificacaoRepository.findByDestinatarioOrderByDataCriacaoDesc(usuario);
    }

    /**
     * Marca uma notificação específica como lida.
     * @param notificacaoId O ID da notificação.
     * @param usuario O usuário que é dono da notificação.
     * @return A notificação atualizada ou null se não for encontrada/pertencer ao usuário.
     */
    @Transactional
    public Notificacao marcarComoLida(Long notificacaoId, Pessoa usuario) {
        return notificacaoRepository.findById(notificacaoId)
                .map(notificacao -> {
                    // Garante que o usuário só pode marcar suas próprias notificações
                    if (!notificacao.getDestinatario().getId().equals(usuario.getId())) {
                        throw new SecurityException("Usuário não autorizado a marcar esta notificação como lida.");
                    }
                    notificacao.setLida(true);
                    return notificacaoRepository.save(notificacao);
                })
                .orElse(null); // Ou lançar uma exceção de "Notificação não encontrada"
    }
}

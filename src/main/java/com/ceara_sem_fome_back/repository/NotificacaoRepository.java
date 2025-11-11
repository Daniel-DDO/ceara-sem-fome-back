
package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    /**
     * Conta o número de notificações não lidas para um destinatário específico.
     * @param destinatario O usuário (Pessoa) destinatário.
     * @return A contagem de notificações não lidas.
     */
    long countByDestinatarioAndLidaIsFalse(Pessoa destinatario);

    /**
     * Busca todas as notificações de um destinatário, ordenadas pela data de criação (mais recentes primeiro).
     * @param destinatario O usuário (Pessoa) destinatário.
     * @return Uma lista de notificações.
     */
    List<Notificacao> findByDestinatarioOrderByDataCriacaoDesc(Pessoa destinatario);
}


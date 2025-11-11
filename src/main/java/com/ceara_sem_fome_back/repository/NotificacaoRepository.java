package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    /**
     * Busca todas as notificações destinadas a um ID de pessoa específico, ordenadas pela data de criação descendente.
     *
     * @param destinatarioId O ID da pessoa (destinatário).
     * @return Uma lista de notificações.
     */
    List<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(String destinatarioId);

}

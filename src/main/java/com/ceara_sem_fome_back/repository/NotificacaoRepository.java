package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(String destinatarioId);
}
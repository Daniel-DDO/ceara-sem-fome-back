package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(String destinatarioId);

    long countByDestinatarioIdAndLidaFalse(String destinatarioId);

    @Modifying
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.destinatarioId = :usuarioId AND n.lida = false")
    void marcarTodasComoLidas(String usuarioId);
}
package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.EventoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoCompraRepository extends JpaRepository<EventoCompra, String> {
    List<EventoCompra> findByCompraIdOrderByDataHoraEventoAsc(String compraId);
}
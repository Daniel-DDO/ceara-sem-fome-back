package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Entregador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntregadorRepository extends JpaRepository <Entregador, String> {
    Entregador findByEmail(String email);
}

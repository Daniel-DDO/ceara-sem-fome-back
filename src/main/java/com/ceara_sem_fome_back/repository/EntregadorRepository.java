package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Entregador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntregadorRepository extends JpaRepository <Entregador, String> {
    Optional<Entregador> findByCpfAndEmail(String cpf, String email);
    Optional<Entregador> findByEmail(String email);
}

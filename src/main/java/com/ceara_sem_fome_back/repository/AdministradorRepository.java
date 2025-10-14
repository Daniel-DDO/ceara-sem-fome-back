package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository <Administrador, String> {
    Optional<Administrador> findByCpfAndEmail(String cpf, String email);
    Optional<Administrador> findByEmail(String email);
}

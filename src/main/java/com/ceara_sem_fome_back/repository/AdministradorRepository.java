package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministradorRepository extends JpaRepository <Administrador, String> {
    Administrador findByLogin(String login);
}

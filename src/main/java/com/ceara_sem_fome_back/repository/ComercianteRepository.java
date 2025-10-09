package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Comerciante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComercianteRepository extends JpaRepository <Comerciante, String> {
    Comerciante findByEmail(String email);
}

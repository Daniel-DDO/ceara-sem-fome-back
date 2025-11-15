package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContaRepository extends JpaRepository<Conta, String> {
    Optional<Conta> findByBeneficiario(Beneficiario beneficiario);
    Optional<Conta> findByComerciante(Comerciante comerciante);
    Optional<Conta> findByComerciante_Id(String comercianteId);
}

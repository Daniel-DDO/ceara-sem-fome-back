package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, String> {
    Optional<Beneficiario> findByEmail(String email);
    // NOVO: Metodo para verificar se um CPF já está cadastrado
    Optional<Beneficiario> findByCpf(String cpf);
    // Usado na redefinição de senha
    Optional<Beneficiario> findByCpfAndEmail(String cpf, String email);

}
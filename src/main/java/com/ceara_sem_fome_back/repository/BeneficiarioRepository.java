package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, String> {
    // Método que a Etapa 1 usará para buscar o beneficiário por CPF e email
    Optional<Beneficiario> findByCpfAndEmail(String cpf, String email);

    // NOVO MÉTODO: A Etapa 3 usará este método para encontrar o usuário
    // com base no email que foi obtido do token.
    Optional<Beneficiario> findByEmail(String email);
}
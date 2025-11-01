package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Comerciante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComercianteRepository extends JpaRepository <Comerciante, String> {
    Optional<Comerciante> findByCpfAndEmail(String cpf, String email);
    Optional<Comerciante> findByEmail(String email);
    Optional<Comerciante> findByCpf(String cpf);

    //MÉTODOS NOVOS E OTIMIZADOS PARA VALIDAÇÃO
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    Page<Comerciante> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
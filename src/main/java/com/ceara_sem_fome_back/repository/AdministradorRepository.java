package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Administrador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository <Administrador, String> {
    Optional<Administrador> findByCpfAndEmail(String cpf, String email);
    Optional<Administrador> findByEmail(String email);
    Optional<Administrador> findByCpf(String cpf);

    //MÉTODOS NOVOS E OTIMIZADOS PARA VALIDAÇÃO
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    Page<Administrador> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    // Metodo para buscar um usuário por ID ignorando o filtro @Where
    @Query("SELECT p FROM #{#entityName} p WHERE p.id = :id")
    Optional<Administrador> findByIdIgnoringStatus(@Param("id") String id);
}
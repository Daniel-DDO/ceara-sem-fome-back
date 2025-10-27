package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Entregador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntregadorRepository extends JpaRepository <Entregador, String> {
    Optional<Entregador> findByCpfAndEmail(String cpf, String email);
    Optional<Entregador> findByEmail(String email);
    Optional<Entregador> findByCpf(String cpf);

    //MÉTODOS NOVOS E OTIMIZADOS PARA VALIDAÇÃO
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    // Método para buscar um usuário por ID ignorando o filtro @Where
    @Query("SELECT p FROM #{#entityName} p WHERE p.id = :id")
    Optional<Entregador> findByIdIgnoringStatus(@Param("id") String id);
}
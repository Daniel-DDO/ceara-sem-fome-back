package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {
    boolean existsProdutoById(String id);

    @Query("SELECT p FROM Produto p WHERE p.id = :id AND p.status = 'AUTORIZADO'")
    Optional<Produto> findById(@Param("id") String id);

    // Metodo para buscar um produto por ID ignorando o filtro @Where
    @Query(value = "SELECT * FROM produto WHERE id = :id", nativeQuery = true)
    Optional<Produto> findByIdIgnoringStatus(@Param("id") String id);

    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.comerciante.id = :comercianteId AND p.status IN ('AUTORIZADO', 'PENDENTE')")
    List<Produto> findByComercianteId(String comercianteId);
}
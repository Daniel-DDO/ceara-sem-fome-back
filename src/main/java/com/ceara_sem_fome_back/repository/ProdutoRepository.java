package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {
    boolean existsProdutoById(String id);

    // Metodo para buscar um produto por ID ignorando o filtro @Where
    @Query("SELECT p FROM Produto p WHERE p.id = :id")
    Optional<Produto> findByIdIgnoringStatus(@Param("id") String id);
}
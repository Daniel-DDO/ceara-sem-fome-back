package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.ProdutoCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutoCarrinhoRepository extends JpaRepository<ProdutoCarrinho, String> {
    
    // Possiveis métodos futuros para a lógica final:
    // Optional<ProdutoCarrinho> findByCarrinhoIdAndProdutoId(String carrinhoId, String produtoId);   
    // void deleteByCarrinhoIdAndProdutoId(String carrinhoId, String produtoId);
}
package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Carrinho;
import com.ceara_sem_fome_back.model.ProdutoCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoCarrinhoRepository extends JpaRepository<ProdutoCarrinho, String> {
    List<ProdutoCarrinho> findByCarrinho(Carrinho carrinho);
    Optional<ProdutoCarrinho> findByCarrinhoIdAndProdutoId(String carrinhoId, String produtoId);
    void deleteByCarrinhoIdAndProdutoId(String carrinhoId, String produtoId);

    List<ProdutoCarrinho> findAllByProdutoId(String produtoId);

    @Modifying
    @Query("delete from ProdutoCarrinho pc where pc.produto.id = :produtoId")
    void deleteAllByProdutoId(@Param("produtoId") String produtoId);
}

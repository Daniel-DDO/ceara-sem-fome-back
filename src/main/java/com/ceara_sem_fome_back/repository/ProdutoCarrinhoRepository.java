package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.ProdutoCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoCarrinhoRepository extends JpaRepository<ProdutoCarrinho, String> {
}

package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Carrinho;
import com.ceara_sem_fome_back.model.StatusCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, String> {
    Optional<Carrinho> findByBeneficiarioEmailAndStatus(String email, StatusCarrinho status);
    List<Carrinho> findByStatus(StatusCarrinho status);
}
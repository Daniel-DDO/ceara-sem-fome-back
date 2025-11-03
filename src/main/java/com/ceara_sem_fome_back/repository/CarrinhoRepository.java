package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, String> {
    // Métodos de busca (ex: findByBeneficiarioId) podem ser adicionados aqui
}
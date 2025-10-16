package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {

}
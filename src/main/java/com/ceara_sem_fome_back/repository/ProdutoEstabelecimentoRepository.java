package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.ProdutoEstabelecimento;
import com.ceara_sem_fome_back.model.ProdutoEstabelecimentoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoEstabelecimentoRepository extends JpaRepository<ProdutoEstabelecimento, ProdutoEstabelecimentoId> {
    List<ProdutoEstabelecimento> findByEstabelecimento_Id(String estabelecimentoId);
}
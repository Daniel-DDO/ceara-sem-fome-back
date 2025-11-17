package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.model.Produto;
import com.ceara_sem_fome_back.model.ProdutoEstabelecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoEstabelecimentoRepository extends JpaRepository<ProdutoEstabelecimento, String> {
    List<ProdutoEstabelecimento> findByEstabelecimento_Id(String estabelecimentoId);
    Page<ProdutoEstabelecimento> findByEstabelecimento_Id(String estabelecimentoId, Pageable pageable);
    Page<ProdutoEstabelecimento> findByEstabelecimento_IdAndProduto_NomeContainingIgnoreCase(String estabelecimentoId, String nome, Pageable pageable);

    boolean existsByProdutoAndEstabelecimento(Produto produto, Estabelecimento estabelecimento);
    Optional<ProdutoEstabelecimento> findByProdutoAndEstabelecimento(Produto produto, Estabelecimento estabelecimento);

    Page<ProdutoEstabelecimento> findByProduto_NomeContainingIgnoreCase(String nome, Pageable pageable);

}
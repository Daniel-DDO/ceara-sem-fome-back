package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, String> {

    @Query("SELECT AVG(a.estrelas) FROM Avaliacao a JOIN a.compra c JOIN c.itensCompra ic WHERE ic.produto.id = :produtoId")
    Double findAverageByProdutoId(String produtoId);

    @Query("SELECT AVG(a.estrelas) FROM Avaliacao a JOIN a.compra c WHERE c.estabelecimento.id = :estabelecimentoId")
    Double findAverageByEstabelecimentoId(String estabelecimentoId);

    @Query("SELECT AVG(a.estrelas) FROM Avaliacao a JOIN a.compra c WHERE c.estabelecimento.comerciante.id = :comercianteId")
    Double findAverageByComercianteId(String comercianteId);

}
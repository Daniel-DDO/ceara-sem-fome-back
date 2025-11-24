package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, String> {

    //Média do Produto (baseado nas compras que contêm este item)
    @Query("""
        SELECT COALESCE(AVG(a.estrelas), 0.0)
        FROM Avaliacao a
        JOIN a.compra c
        JOIN c.itens ic
        WHERE ic.produtoEstabelecimento.id = :produtoEstabelecimentoId
    """)
    Double findAverageByProdutoEstabelecimentoId(String produtoEstabelecimentoId);

    //Média do Estabelecimento (navegando pelos itens da compra)
    @Query("""
        SELECT COALESCE(AVG(a.estrelas), 0.0)
        FROM Avaliacao a 
        JOIN a.compra c 
        JOIN c.itens ic 
        JOIN ic.produtoEstabelecimento pe 
        WHERE pe.estabelecimento.id = :estabelecimentoId
    """)
    Double findAverageByEstabelecimentoId(String estabelecimentoId);

    //Média do Comerciante (navegando itens -> prodEst -> estab -> comerciante)
    @Query("""
        SELECT COALESCE(AVG(a.estrelas), 0.0)
        FROM Avaliacao a 
        JOIN a.compra c 
        JOIN c.itens ic 
        JOIN ic.produtoEstabelecimento pe 
        JOIN pe.estabelecimento e 
        WHERE e.comerciante.id = :comercianteId
    """)
    Double findAverageByComercianteId(String comercianteId);
}
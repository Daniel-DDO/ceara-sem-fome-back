package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.model.ProdutoCompra;
import com.ceara_sem_fome_back.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProdutoCompraRepository extends JpaRepository<ProdutoCompra, String> {
    List<ProdutoCompra> findByCompra(Compra compra);
    List<ProdutoCompra> findByProdutoEstabelecimento_Estabelecimento(Estabelecimento estabelecimento);
}

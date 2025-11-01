package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.ItemCompra;
import com.ceara_sem_fome_back.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemCompraRepository extends JpaRepository<ItemCompra, String> {
    List<ItemCompra> findByCompra(Compra compra);
}

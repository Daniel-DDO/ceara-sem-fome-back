package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Compra;
import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.model.StatusCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, String> {

    List<Compra> findByBeneficiario(Beneficiario beneficiario);
    List<Compra> findByEstabelecimento(Estabelecimento estabelecimento);
    List<Compra> findByStatus(StatusCompra status);
    List<Compra> findByDataHoraCompraAfter(LocalDateTime data);
    List<Compra> findByDataHoraCompraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Compra> findByEstabelecimentoIdAndStatus(String estabelecimentoId, StatusCompra status);
    List<Compra> findByEstabelecimentoId(String estabelecimentoId);
    List<Compra> findByEstabelecimentoComercianteId(String comercianteId);
    Page<Compra> findByBeneficiarioNomeContainingIgnoreCase(String nome, Pageable pageable);
}

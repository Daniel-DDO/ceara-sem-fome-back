package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, String> {

    List<Compra> findByBeneficiario(Beneficiario beneficiario);
    List<Compra> findByStatus(StatusCompra status);
    List<Compra> findByDataHoraCompraAfter(LocalDateTime data);
    List<Compra> findByDataHoraCompraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Compra> findByBeneficiarioOrderByDataHoraCompraDesc(Beneficiario beneficiario);
    List<Compra> findByBeneficiarioId(String beneficiarioId);
    List<Compra> findDistinctByItensProdutoEstabelecimentoEstabelecimento(Estabelecimento estabelecimento);
    List<Compra> findDistinctByItensProdutoEstabelecimentoEstabelecimentoId(String estabelecimentoId);
    List<Compra> findDistinctByItensProdutoEstabelecimentoEstabelecimentoComerciante(Comerciante comerciante);
    List<Compra> findDistinctByItensProdutoEstabelecimentoEstabelecimentoComercianteId(String comercianteId);
}

package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProdutoEstabelecimento {

    //chave composta: combinação do ID do produto e do estabelecimento
    @EmbeddedId
    private ProdutoEstabelecimentoId id;

    @ManyToOne
    @MapsId("produtoId")
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @MapsId("estabelecimentoId")
    @JoinColumn(name = "estabelecimento_id")
    private Estabelecimento estabelecimento;

    @NotNull
    private BigDecimal precoVenda;

    @Min(0)
    private Integer estoque;
}
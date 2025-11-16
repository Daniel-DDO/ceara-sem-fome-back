package com.ceara_sem_fome_back.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class CompraItemDTO {
    private String produtoNome;
    private Integer quantidade;
    private BigDecimal valorUnitario;

    public CompraItemDTO(String produtoNome, Integer quantidade, BigDecimal valorUnitario) {
        this.produtoNome = produtoNome;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }
}

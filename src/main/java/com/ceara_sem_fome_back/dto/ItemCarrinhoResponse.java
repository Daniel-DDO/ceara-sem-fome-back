package com.ceara_sem_fome_back.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemCarrinhoResponse {
    private String id;
    private String produtoId;
    private String produtoNome;
    private String lote;
    private String descricao;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
    private Integer quantidadeEstoque;
    private String statusProduto;
    private String comercianteId;

}
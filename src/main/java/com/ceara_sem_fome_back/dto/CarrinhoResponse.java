package com.ceara_sem_fome_back.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CarrinhoResponse {
    private String id;
    private String status;
    private LocalDateTime criacao;
    private LocalDateTime modificacao;
    private BigDecimal subtotal;
    private List<ItemCarrinhoResponse> itens;
}
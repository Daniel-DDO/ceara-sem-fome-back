package com.ceara_sem_fome_back.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompraRequestDTO {
    private List<ProdutoCarrinhoRequestDTO> itens;
}


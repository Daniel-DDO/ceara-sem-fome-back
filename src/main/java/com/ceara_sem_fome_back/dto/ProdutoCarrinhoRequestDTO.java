package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para requisições de adicionar ou atualizar
 * um item no carrinho.
 */
@Data
public class ProdutoCarrinhoRequestDTO {

    @NotBlank(message = "O ID do produto_estabelecimento é obrigatório.")
    private String produtoEstabelecimentoId;

    @NotNull(message = "A quantidade é obrigatória.")
    @Min(value = 1, message = "A quantidade deve ser pelo menos 1.")
    private Integer quantidade;
}
package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProdutoCadastroRequest {

    @NotBlank
    private String nome;

    @NotBlank(message = "O ID do estabelecimento é obrigatório.")
    private String estabelecimentoId;

    @NotNull(message = "O preço de venda é obrigatório.")
    private BigDecimal precoVenda;

    @NotNull(message = "O estoque é obrigatório.")
    private Integer estoque;

}
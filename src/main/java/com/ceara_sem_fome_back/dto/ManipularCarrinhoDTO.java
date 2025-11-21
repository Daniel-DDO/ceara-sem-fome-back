package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManipularCarrinhoDTO {

    @NotBlank(message = "O ID do produto é obrigatório.")
    private String produtoId;

    @Min(value = 0, message = "A quantidade deve ser zero (para remover) ou maior.")
    private int quantidade;
}

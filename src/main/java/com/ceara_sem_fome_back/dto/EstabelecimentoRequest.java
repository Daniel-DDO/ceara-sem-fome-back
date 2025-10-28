package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstabelecimentoRequest {

    @NotBlank(message = "O nome do estabelecimento é obrigatório.")
    private String nome;

    @NotBlank(message = "O ID do comerciante é obrigatório.")
    private String comercianteId;

    private String enderecoId;
}

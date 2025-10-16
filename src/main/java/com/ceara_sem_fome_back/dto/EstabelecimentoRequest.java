package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstabelecimentoRequest {

    @NotBlank(message = "O ID (CNPJ/CPF) é obrigatório.")
    private String id; // Usado como identificador e chave primária

    @NotBlank(message = "O nome do estabelecimento é obrigatório.")
    private String nome;

    @NotBlank(message = "O CPF do Comerciante é obrigatório.")
    private String comercianteCpf;

}
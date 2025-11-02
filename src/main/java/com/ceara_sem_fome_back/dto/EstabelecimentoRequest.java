package com.ceara_sem_fome_back.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class EstabelecimentoRequest {

    @NotBlank(message = "O nome do estabelecimento é obrigatório.")
    private String nome;

    private String cnpj;

    private String telefone; //do comércio

    private LocalDateTime dataCadastro;

    private String enderecoId;
}

package com.ceara_sem_fome_back.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BeneficiarioRespostaDTO {

    private String id;

    private String nome;
    private String cpf;
    private String email;
    private LocalDate dataNascimento;
    private String telefone;
    private String genero;

    private Boolean lgpdAccepted;

    private String numeroCadastroSocial;

    private EnderecoRespostaDTO endereco;  // DTO do endereço
}

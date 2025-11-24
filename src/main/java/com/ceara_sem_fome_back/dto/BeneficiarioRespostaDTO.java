package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.Conta;
import com.ceara_sem_fome_back.model.StatusPessoa;
import jdk.jshell.Snippet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    private Conta conta;

    private StatusPessoa status;

}

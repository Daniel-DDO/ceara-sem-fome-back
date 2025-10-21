package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
//import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
public class EntregadorRequest implements CadastroRequest {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @Size(min = 11, max = 11, message = "O CPF deve ter 11 dígitos.")
    @NotBlank(message = "O CPF é obrigatório.")
    private String cpf;

    @Email(message = "Email inválido.")
    @NotBlank(message = "O email é obrigatório.")
    private String email;

    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    @NotBlank(message = "A senha é obrigatória.")
    private String senha;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve ser no passado.")
    private LocalDate dataNascimento;

    @NotBlank(message = "O telefone é obrigatório.")
    private String telefone;

    @NotBlank(message = "O gênero é obrigatório.")
    private String genero;

    // [NOVO] Adicionamos o campo de consentimento da LGPD
    @NotNull(message = "É preciso confirmar os termos da LGPD.")
    @AssertTrue(message = "É preciso aceitar os termos da LGPD para continuar.")
    private Boolean lgpdAccepted;
}
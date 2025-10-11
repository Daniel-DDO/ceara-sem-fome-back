package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
public class EntregadorRequest {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @CPF(message = "CPF inválido.")
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

}
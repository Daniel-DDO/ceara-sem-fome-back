package com.ceara_sem_fome_back.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class Pessoa {

    @Id
    protected String id;
    @NotBlank
    protected String nome;
    @CPF
    protected String cpf;
    @Email
    protected String email;
    @NotBlank
    protected String senha;
    @NotNull
    protected LocalDate dataNascimento;
    @NotBlank
    protected String telefone;
    @NotNull
    protected String genero;

    public Pessoa(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.genero = genero;
    }

    public Pessoa() {}
}

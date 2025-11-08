package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@Where(clause = "pessoa.status = 'ATIVO'")
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;
    @NotBlank
    protected String nome;
    @Column(unique = true)
    @CPF
    protected String cpf;
    @Column(unique = true)
    @Email
    protected String email;
    @NotBlank
    protected String senha;
    @NotNull
    protected LocalDate dataNascimento;
    @NotBlank
    protected String telefone;
    @NotBlank
    protected String genero;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusPessoa status;
    @NotNull
    protected Boolean lgpdAccepted;

    public Pessoa(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero, Boolean lgpdAccepted) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.senha = senha;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.genero = genero;
        this.status = StatusPessoa.ATIVO;
        this.lgpdAccepted = lgpdAccepted;
    }

}
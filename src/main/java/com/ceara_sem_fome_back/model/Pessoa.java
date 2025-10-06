package com.ceara_sem_fome_back.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public abstract class Pessoa {
    protected String id;
    protected String nome;
    protected String cpf;
    protected String email;
    protected String senha;
    protected LocalDate dataNascimento;
    protected String telefone;
    protected String genero;

    public Pessoa(String id, String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero) {
        this.id = id;
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

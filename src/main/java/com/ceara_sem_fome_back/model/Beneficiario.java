package com.ceara_sem_fome_back.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Beneficiario extends Pessoa {
    public Beneficiario(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero);
    }

    public Beneficiario() {
        super();
    }

}

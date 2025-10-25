package com.ceara_sem_fome_back.model;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Administrador extends Pessoa {

    public Administrador(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero, Boolean lgpdAccepted) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero, lgpdAccepted);
    }

}

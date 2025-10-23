package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Comerciante extends Pessoa {

    @OneToMany(mappedBy = "comerciante", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Estabelecimento> estabelecimentos;

    public Comerciante(String nome, String cpf, String email, String senha,
                       LocalDate dataNascimento, String telefone, String genero) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero);
        //@OneToMany(mappedBy = "comerciante", fetch = FetchType.LAZY)
        List<Estabelecimento> estabelecimentos = new ArrayList<>();
    }
}

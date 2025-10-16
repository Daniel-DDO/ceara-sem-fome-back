package com.ceara_sem_fome_back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@Entity
public class Comerciante extends Pessoa {

    @OneToMany(mappedBy = "comerciante", fetch = FetchType.LAZY)
    private List<Estabelecimento> estabelecimentos = new ArrayList<>();

    public Comerciante(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero);
    }

    public Comerciante() {
        super();
    }
}

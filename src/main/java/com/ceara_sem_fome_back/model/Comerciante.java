package com.ceara_sem_fome_back.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Comerciante extends Pessoa {

    @OneToMany(mappedBy = "comerciante", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference
    private List<Estabelecimento> estabelecimentos = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "conta_id", unique = true)
    private Conta conta;

    private Double mediaAvaliacoes = 0.0;

    public Comerciante(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero, Boolean lgpdAccepted) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero, lgpdAccepted);
        this.conta = new Conta();
        setStatus(StatusPessoa.PENDENTE);
    }

    public Comerciante() {
        super();
        this.conta = new Conta();
        setStatus(StatusPessoa.PENDENTE);
    }
}

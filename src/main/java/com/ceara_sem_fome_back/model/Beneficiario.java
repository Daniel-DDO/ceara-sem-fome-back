package com.ceara_sem_fome_back.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Beneficiario extends Pessoa {

    private String numeroCadastroSocial;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "conta_id", unique = true)
    private Conta conta;

    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    @JoinColumn(name = "carrinho_id")
    @JsonManagedReference
    private Carrinho carrinho;

    @OneToMany(mappedBy = "beneficiario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Compra> compras;

    @OneToOne(cascade = CascadeType.ALL, optional = true, orphanRemoval = true)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    public Beneficiario(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero, Boolean lgpdAccepted) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero, lgpdAccepted);
        this.carrinho = new Carrinho();
        this.compras = new ArrayList<>();
        this.conta = new Conta();
    }

    public Beneficiario() {
        super();
        this.carrinho = new Carrinho();
        this.compras = new ArrayList<>();
        this.conta = new Conta();
    }
}

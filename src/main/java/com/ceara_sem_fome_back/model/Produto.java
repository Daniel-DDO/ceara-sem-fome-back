package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Produto {
    @Id
    private String id;
    @NotBlank
    private String nome;
    private String lote;
    private String descricao;
    private double preco;
    private int quantidadeEstoque;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProdutoEstabelecimento> estabelecimentos = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_cpf")
    //quem adicionou o produto
    private Comerciante criador;

    public Produto() {}
}

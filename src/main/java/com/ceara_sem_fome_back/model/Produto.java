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
    @Enumerated(EnumType.STRING)
    private StatusProduto status;

    @Lob
    @Column(name = "imagem")
    private byte[] imagem;
    private String tipoImagem;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProdutoEstabelecimento> estabelecimentos = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_id")
    //quem adicionou o produto
    private Comerciante criador;

    public Produto() {}

    public Produto(String id, String nome, String lote, String descricao, double preco, int quantidadeEstoque, StatusProduto status, Set<ProdutoEstabelecimento> estabelecimentos, Comerciante criador) {
        this.id = id;
        this.nome = nome;
        this.lote = lote;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.status = status;
        this.estabelecimentos = estabelecimentos;
        this.criador = criador;
    }
}

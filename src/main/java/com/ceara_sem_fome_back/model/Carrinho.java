package com.ceara_sem_fome_back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private StatusCarrinho status = StatusCarrinho.ABERTO;
    private LocalDateTime criacao = LocalDateTime.now();
    private LocalDateTime modificacao = LocalDateTime.now();

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @OneToOne(mappedBy = "carrinho")
    @JsonBackReference
    private Beneficiario beneficiario;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProdutoCarrinho> produtos = new ArrayList<>();

    public void atualizarSubtotal() {
        this.subtotal = produtos.stream()
                .map(item -> item.getProdutoEstabelecimento().getProduto().getPreco()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.modificacao = LocalDateTime.now();
    }

    public void esvaziarCarrinho() {
        produtos.clear();
        this.subtotal = BigDecimal.ZERO;
        this.status = StatusCarrinho.FINALIZADO;
        this.modificacao = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.modificacao = LocalDateTime.now();
    }

    public void adicionarProduto(ProdutoEstabelecimento produtoEstabelecimento, int quantidade) {
        for (ProdutoCarrinho item : produtos) {
            if (item.getProdutoEstabelecimento().getProduto().equals(produtoEstabelecimento)) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                atualizarSubtotal();
                return;
            }
        }

        ProdutoCarrinho novoItem = new ProdutoCarrinho();
        novoItem.setCarrinho(this);
        novoItem.setProdutoEstabelecimento(produtoEstabelecimento);
        novoItem.setQuantidade(quantidade);
        produtos.add(novoItem);
        atualizarSubtotal();
    }

    public void removerProduto(ProdutoEstabelecimento produtoEstabelecimento) {
        produtos.removeIf(item -> item.getProdutoEstabelecimento().getProduto().equals(produtoEstabelecimento));
        atualizarSubtotal();
    }
}

package com.ceara_sem_fome_back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "produto_carrinho")
public class ProdutoCarrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "carrinho_id")
    @JsonBackReference
    private Carrinho carrinho;

    @ManyToOne
    @JoinColumn(name = "produto_estabelecimento_id")
    private ProdutoEstabelecimento produtoEstabelecimento;

    private Integer quantidade;
}

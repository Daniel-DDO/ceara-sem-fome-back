package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @ManyToOne
    @JoinColumn(name = "comerciante_id")
    private Comerciante comerciante;

}

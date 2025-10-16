package com.ceara_sem_fome_back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

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

    public Produto() {}
}

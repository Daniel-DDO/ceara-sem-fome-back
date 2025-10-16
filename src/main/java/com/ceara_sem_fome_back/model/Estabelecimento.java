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
public class Estabelecimento {
    @Id
    private String id;
    @NotBlank
    private String nome;

    //relacionamento com o comerciante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_cpf", nullable = false)
    private Comerciante comerciante;

    //relacionamento com os produtos
    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProdutoEstabelecimento> produtos = new HashSet<>();

    public Estabelecimento() {}
}

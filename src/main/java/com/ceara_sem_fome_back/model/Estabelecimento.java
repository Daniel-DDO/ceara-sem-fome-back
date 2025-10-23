package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String nome;
    private String endereco;

    //@ManyToOne
    //@JoinColumn(name = "comerciante_id", nullable = false)
    //private Comerciante comerciante;

    //relacionamento com o comerciante
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comerciante_cpf", nullable = false)
    private Comerciante comerciante;

    //relacionamento com os produtos
    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProdutoEstabelecimento> produtos = new HashSet<>();

    public Estabelecimento() {}

}

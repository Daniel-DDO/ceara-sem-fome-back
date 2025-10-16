package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String nome;
    private String endereco;

    @ManyToOne
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;
}

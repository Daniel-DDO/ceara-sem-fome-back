package com.ceara_sem_fome_back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
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

    @NotBlank
    private String nome;
    private String endereco;

    @ManyToOne
    @JoinColumn(name = "comerciante_id", nullable = false)
    private Comerciante comerciante;
}

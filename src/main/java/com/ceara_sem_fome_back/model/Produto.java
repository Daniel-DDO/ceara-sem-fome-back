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

    public Produto() {}
}

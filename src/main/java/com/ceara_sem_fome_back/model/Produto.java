package com.ceara_sem_fome_back.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Produto {
    @Id
    private String id;
    private String nome;

    public Produto() {}
}

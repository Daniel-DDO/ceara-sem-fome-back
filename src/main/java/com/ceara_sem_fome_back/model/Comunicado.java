package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Comunicado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "administrador_id", nullable = false)
    private Administrador administrador;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String mensagem;

    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private CategoriaComunicado categoria;

    private Boolean ativo = true;

    @PrePersist
    public void prePersist() {
        this.dataHora = LocalDateTime.now();
    }
}

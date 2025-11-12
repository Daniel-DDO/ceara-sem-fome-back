package com.ceara_sem_fome_back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evento_compra")
public class EventoCompra {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @JsonBackReference
    private Compra compra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCompra status;

    @Column(nullable = false)
    private LocalDateTime dataHoraEvento;

    private String descricao;

    public EventoCompra(Compra compra, StatusCompra status, String descricao) {
        this.id = UUID.randomUUID().toString();
        this.compra = compra;
        this.status = status;
        this.descricao = descricao;
        this.dataHoraEvento = LocalDateTime.now();
    }
}
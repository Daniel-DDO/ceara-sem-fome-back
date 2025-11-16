package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Entity
public class Conta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String numeroConta;
    private String agencia;
    @Column(precision = 18, scale = 2)
    private BigDecimal saldo;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private boolean ativa;

    public Conta() {
        this.saldo = BigDecimal.valueOf(0.0);
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
        this.ativa = true;
    }
}

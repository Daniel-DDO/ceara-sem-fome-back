package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Conta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String numeroConta;
    private String agencia;
    private BigDecimal saldo;

    @OneToOne
    @JoinColumn(name = "beneficiario_id", nullable= true)
    private Beneficiario beneficiario;

    @OneToOne
    @JoinColumn(name = "comerciante_id", nullable= true)
    private Comerciante comerciante;
}

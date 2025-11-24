package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime dataHoraCompra;

    private Double valorTotal;

    @ManyToOne
    @JoinColumn(name = "beneficiario_id", nullable = false)
    private Beneficiario beneficiario;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<ProdutoCompra> itens;

    @Enumerated(EnumType.STRING)
    private StatusCompra status;

    private boolean avaliada;

    public Compra(Beneficiario beneficiario, Double valorTotal) {
        this.id = UUID.randomUUID().toString();
        this.dataHoraCompra = LocalDateTime.now();
        this.beneficiario = beneficiario;
        this.valorTotal = valorTotal;
        this.status = StatusCompra.ABERTA;
    }
}

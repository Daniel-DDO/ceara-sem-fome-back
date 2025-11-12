package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;

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

    @ManyToOne
    @JoinColumn(name = "estabelecimento_id", nullable = false)
    private Estabelecimento estabelecimento;

    @ManyToOne
    @JoinColumn(name = "endereco_id")
    private Endereco endereco; 

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    private List<ItemCompra> itens;

    @Enumerated(EnumType.STRING)
    private StatusCompra status;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EventoCompra> eventos = new ArrayList<>();

    public Compra(Beneficiario beneficiario, Estabelecimento estabelecimento, Endereco endereco, Double valorTotal) {
        this.id = UUID.randomUUID().toString();
        this.dataHoraCompra = LocalDateTime.now();
        this.beneficiario = beneficiario;
        this.estabelecimento = estabelecimento;
        this.endereco = endereco;
        this.valorTotal = valorTotal;
        this.status = StatusCompra.FINALIZADA;
    }
}

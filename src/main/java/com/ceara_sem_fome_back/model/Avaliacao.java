package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", unique = true, nullable = false)
    private Compra compra;

    @Column(nullable = false)
    private Integer estrelas; // 1 a 5

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime dataAvaliacao = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String respostaComerciante;

    private LocalDateTime dataResposta;

}

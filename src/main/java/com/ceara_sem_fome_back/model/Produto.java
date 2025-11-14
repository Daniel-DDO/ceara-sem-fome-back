package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Where(clause = "status IN ('AUTORIZADO', 'PENDENTE')")
public class Produto {
    @Id
    private String id;

    @NotBlank
    private String nome;
    private String lote;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    private int quantidadeEstoque;

    @Enumerated(EnumType.STRING)
    private StatusProduto status;

    @Column(name = "imagem", columnDefinition = "TEXT")
    private String imagem;
    private String tipoImagem;

    @ManyToOne
    @JoinColumn(name = "comerciante_id")
    private Comerciante comerciante;

    @Enumerated(EnumType.STRING)
    private CategoriaProduto categoria;

    @Enumerated(EnumType.STRING)
    private UnidadeProduto unidade;

    private LocalDateTime dataCadastro;

    @ManyToOne
    @JoinColumn(name = "avaliado_por_id")
    private Administrador avaliadoPorId;

    private LocalDateTime dataAvaliacao;
}
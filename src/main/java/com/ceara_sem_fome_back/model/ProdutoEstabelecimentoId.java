package com.ceara_sem_fome_back.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoEstabelecimentoId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String produtoId;
    private String estabelecimentoId;
}
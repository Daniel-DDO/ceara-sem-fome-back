
package com.ceara_sem_fome_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoVendasDTO {

    private String compraId;
    private String produtoEstabelecimentoId;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private LocalDateTime dataCompra;
    private String beneficiarioId;

}

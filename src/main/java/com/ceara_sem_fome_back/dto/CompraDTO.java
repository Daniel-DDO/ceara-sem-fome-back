package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.StatusCompra;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompraDTO {

    private String compraId;
    private LocalDateTime dataCompra;
    private String beneficiarioId;
    private String beneficiarioNome;
    private String estabelecimentoId;
    private String nomeEstabelecimento;
    private List<ProdutoCompraDTO> itens;
    private StatusCompra status;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoCompraDTO {
        private String produtoId;
        private String produtoEstabelecimentoId;
        private String produtoNome;
        private Integer quantidade;
        private BigDecimal valorUnitario;
        private BigDecimal subtotal;
    }
}

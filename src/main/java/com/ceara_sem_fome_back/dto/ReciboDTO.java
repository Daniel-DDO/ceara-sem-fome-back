package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.ItemCompra;
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
public class ReciboDTO {
    private String compraId;
    private LocalDateTime dataCompra;
    private String nomeBeneficiario;
    private String beneficiarioId;
    private String nomeComerciante;
    private String nomeEstabelecimento;
    private String enderecoEstabelecimentoCompleto;
    private Double latitude;
    private Double longitude;
    private List<ItemCompraDTO> itens;
    private BigDecimal valorTotal;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemCompraDTO {
        private String nomeProduto;
        private int quantidade;
        private BigDecimal valorUnitario;
        private BigDecimal subtotal;
    }
}

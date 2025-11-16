package com.ceara_sem_fome_back.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class CompraRespostaDTO {

    private String id;
    private LocalDateTime dataHora;
    private Double valorTotal;
    private String beneficiarioNome;
    private String estabelecimentoNome;
    private String endereco;
    private List<CompraItemDTO> itens;

    public CompraRespostaDTO(String id, LocalDateTime dataHora, Double valorTotal,
                             String beneficiarioNome, String estabelecimentoNome,
                             String endereco, List<CompraItemDTO> itens) {
        this.id = id;
        this.dataHora = dataHora;
        this.valorTotal = valorTotal;
        this.beneficiarioNome = beneficiarioNome;
        this.estabelecimentoNome = estabelecimentoNome;
        this.endereco = endereco;
        this.itens = itens;
    }
}

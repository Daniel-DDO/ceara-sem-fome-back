package com.ceara_sem_fome_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasRegionaisDTO {

    private String regiaoNome; // Nome do município ou bairro
    private String tipoRegiao; // "Município" ou "Bairro"
    private long totalBeneficiarios;
    private long totalCompras;
    private long totalEntregadores;
    
}
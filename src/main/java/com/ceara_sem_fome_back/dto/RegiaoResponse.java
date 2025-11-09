package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Estabelecimento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegiaoResponse {
    private String municipio;
    private String bairro;
    private List<Beneficiario> beneficiarios;
    private List<Comerciante> comerciantes;
    private List<Estabelecimento> estabelecimentos;

    public RegiaoResponse(String municipio, String bairro, List<Beneficiario> beneficiarios, List<Estabelecimento> estabelecimentos) {
        this.municipio = municipio;
        this.bairro = bairro;
        this.beneficiarios = beneficiarios;
        this.estabelecimentos = estabelecimentos;
    }
}

package com.ceara_sem_fome_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoRespostaDTO {

    private String id;
    private String cep;
    private String logradouro;
    private String numero;
    private String bairro;
    private String municipio;
}

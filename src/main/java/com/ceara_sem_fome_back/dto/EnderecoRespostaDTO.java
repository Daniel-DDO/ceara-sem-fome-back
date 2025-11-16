package com.ceara_sem_fome_back.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnderecoRespostaDTO {

    private String id;
    private String cep;
    private String logradouro;
    private String numero;
    private String bairro;
    private String municipio;
}

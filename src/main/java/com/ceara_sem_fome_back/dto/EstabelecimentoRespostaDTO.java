package com.ceara_sem_fome_back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstabelecimentoRespostaDTO {

    private String id;
    private String nome;
    private String cnpj;
    private String telefone;
    private String logradouro;
    private String numero;
    private String bairro;
    private String municipio;

    public EstabelecimentoRespostaDTO(String id, String nome, String cnpj, String telefone,
                                      String logradouro, String numero, String bairro, String municipio) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.logradouro = logradouro;
        this.numero = numero;
        this.bairro = bairro;
        this.municipio = municipio;
    }
}

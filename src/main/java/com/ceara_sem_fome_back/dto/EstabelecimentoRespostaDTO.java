package com.ceara_sem_fome_back.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstabelecimentoRespostaDTO {

    private String id;
    private String nome;
    private String cnpj;
    private String telefone;
    private String imagem;
    private String tipoImagem;
    private Double mediaAvaliacoes;
    private String enderecoId;
    private String cep;
    private String logradouro;
    private String numero;
    private String bairro;
    private String municipio;
    private Double latitude;
    private Double longitude;
    private String comercianteId;
    private String comercianteNome;

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

package com.ceara_sem_fome_back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PessoaRespostaDTO {

    private String id;
    private String nome;
    private String email;
    private String token;

    public PessoaRespostaDTO(String id, String nome, String email, String token) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.token = token;
    }
}

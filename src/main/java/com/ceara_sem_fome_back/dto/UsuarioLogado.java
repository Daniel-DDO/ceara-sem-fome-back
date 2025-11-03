package com.ceara_sem_fome_back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioLogado {

    private String id;
    private String nome;
    private String email;

    public UsuarioLogado(String id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }
}

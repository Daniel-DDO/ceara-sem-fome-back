package com.ceara_sem_fome_back.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Entregador extends Pessoa {
    //Essa classe é apenas um molde por agora. Só será utilizada quando formos fazer essa parte de entregas.

    public Entregador(String id, String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero) {
        super(id, nome, cpf, email, senha, dataNascimento, telefone, genero);
    }

    public Entregador() {
        super();
    }
}

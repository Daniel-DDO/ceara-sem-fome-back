package com.ceara_sem_fome_back.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Entregador extends Pessoa {
    //Essa classe é apenas um molde por agora. Só será utilizada quando formos fazer essa parte de entregas.

    @OneToOne(cascade = CascadeType.ALL, optional = true)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    public Entregador(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero, Boolean lgpdAccepted) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero, lgpdAccepted);
    }

}

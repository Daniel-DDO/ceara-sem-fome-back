package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EqualsAndHashCode(callSuper = true)
public class Beneficiario extends Pessoa {

   
    private String numeroCadastroSocial;

    @OneToOne(mappedBy = "beneficiario", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Conta conta;

    public Beneficiario(String nome, String cpf, String email, String senha,
                        LocalDate dataNascimento, String telefone, String genero) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero);
    }
}

package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Comerciante extends Pessoa {

    @OneToMany(mappedBy = "comerciante", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Estabelecimento> estabelecimentos;

    public Comerciante(String nome, String cpf, String email, String senha,
                       LocalDate dataNascimento, String telefone, String genero) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero);
    }
}

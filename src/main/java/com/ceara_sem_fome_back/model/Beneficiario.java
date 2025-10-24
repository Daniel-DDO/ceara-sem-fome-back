package com.ceara_sem_fome_back.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class Beneficiario extends Pessoa {

    private String numeroCadastroSocial;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "carrinho_id")
    @JsonManagedReference
    private Carrinho carrinho;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    public Beneficiario(String nome, String cpf, String email, String senha,
                        LocalDate dataNascimento, String telefone, String genero) {
        super(nome, cpf, email, senha, dataNascimento, telefone, genero);
    }
}

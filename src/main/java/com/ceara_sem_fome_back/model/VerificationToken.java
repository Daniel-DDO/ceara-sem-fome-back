package com.ceara_sem_fome_back.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "verification_token")
public class VerificationToken {

    @Id
    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    // Campos adicionais para o cadastro
    @Column(name = "nome")
    private String nome;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "senha_criptografada")
    private String senhaCriptografada;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "genero")
    private String genero;

    @Enumerated(EnumType.STRING)
    private TipoPessoa tipoPessoa;

    /**
     * Construtor para criar um "token rico" com todos os dados do cadastro.
     * Este é o construtor que estava faltando.
     */
    public VerificationToken(String token, String nome, String cpf, String userEmail, String senhaCriptografada,
                             LocalDate dataNascimento, String telefone, String genero, TipoPessoa tipoPessoa) {
        this.token = token;
        this.nome = nome;
        this.cpf = cpf;
        this.userEmail = userEmail;
        this.senhaCriptografada = senhaCriptografada;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.genero = genero;
        this.expiryDate = LocalDateTime.now().plusMinutes(15); // Define a expiração padrão para cadastro
        this.tipoPessoa = tipoPessoa;
    }
}


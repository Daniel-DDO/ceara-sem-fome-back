package com.ceara_sem_fome_back.dto;

import java.time.LocalDate;

public interface CadastroRequest {
    String getNome();
    String getCpf();
    String getEmail();
    String getSenha();
    LocalDate getDataNascimento();
    String getTelefone();
    String getGenero();
    Boolean getLgpdAccepted();
}

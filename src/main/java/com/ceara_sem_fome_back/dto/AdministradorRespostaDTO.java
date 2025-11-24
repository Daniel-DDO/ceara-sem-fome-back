package com.ceara_sem_fome_back.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AdministradorRespostaDTO {

    private String id;
    private String nome;
    private String cpf;
    private String email;
    private LocalDate dataNascimento;
    private String telefone;
    private String genero;
    private Boolean lgpdAccepted;

}


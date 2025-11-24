package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.Conta;
import com.ceara_sem_fome_back.model.StatusPessoa;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ComercianteRespostaDTO {

    private String id;

    private String nome;
    private String cpf;
    private String email;
    private LocalDate dataNascimento;
    private String telefone;
    private String genero;
    private Boolean lgpdAccepted;
    private StatusPessoa status;
    private Conta conta;

    private List<EstabelecimentoResumoResponse> estabelecimentos;
}

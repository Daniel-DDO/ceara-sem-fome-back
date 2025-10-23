package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.TipoPessoa;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecuperacaoSenhaDTO {
    // Campos necessários para a verificação cruzada
    private String cpf;
    private String email;
}
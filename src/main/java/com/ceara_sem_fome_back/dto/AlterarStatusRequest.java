package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.StatusPessoa;
import com.ceara_sem_fome_back.model.TipoPessoa;
import lombok.Data;

@Data
public class AlterarStatusRequest {
    private String id;
    private StatusPessoa novoStatusPessoa;
    private TipoPessoa tipoPessoa;
}

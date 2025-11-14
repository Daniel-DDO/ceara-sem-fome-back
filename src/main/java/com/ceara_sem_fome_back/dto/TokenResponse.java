package com.ceara_sem_fome_back.dto;

import lombok.Data;

@Data
public class TokenResponse {
    private String status;
    private String mensagem;

    public TokenResponse(String status, String mensagem) {
        this.status = status;
        this.mensagem = mensagem;
    }
}

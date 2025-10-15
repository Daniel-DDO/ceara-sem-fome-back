package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;

public class CpfInvalidoException extends NegocioException {
    private static final String MENSAGEM_PADRAO = "O CPF informado é inválido.";

    public CpfInvalidoException(String cpf) {
        super(MENSAGEM_PADRAO + " CPF: " + cpf, HttpStatus.BAD_REQUEST);
    }
}

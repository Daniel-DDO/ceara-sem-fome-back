package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;

public class RecursoNaoEncontradoException extends NegocioException {
    private static final String MENSAGEM_PADRAO = "O recurso solicitado não foi encontrado.";

    public RecursoNaoEncontradoException() {
        super(MENSAGEM_PADRAO, HttpStatus.NOT_FOUND); // 404
    }

    public RecursoNaoEncontradoException(String detalhe) {
        super(MENSAGEM_PADRAO + " Detalhe: " + detalhe, HttpStatus.NOT_FOUND);
    }
}

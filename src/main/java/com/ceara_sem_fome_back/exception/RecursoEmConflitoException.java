package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;

public class RecursoEmConflitoException extends NegocioException {
    private static final String MENSAGEM_PADRAO = "Já existe um registro com dados de identificação conflitantes.";

    public RecursoEmConflitoException() {
        super(MENSAGEM_PADRAO, HttpStatus.CONFLICT); // 409
    }

    public RecursoEmConflitoException(String detalhe) {
        super(MENSAGEM_PADRAO + " Detalhe: " + detalhe, HttpStatus.CONFLICT);
    }
}

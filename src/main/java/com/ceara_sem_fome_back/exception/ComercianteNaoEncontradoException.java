package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ComercianteNaoEncontradoException extends RuntimeException {
    public ComercianteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
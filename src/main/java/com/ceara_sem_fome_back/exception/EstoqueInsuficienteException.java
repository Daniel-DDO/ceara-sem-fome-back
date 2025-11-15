package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;

//status 409 CONFLICT
public class EstoqueInsuficienteException extends NegocioException {
    private static final String MENSAGEM_PADRAO = "Estoque insuficiente para o produto solicitado.";

    public EstoqueInsuficienteException(String detalhe) {
        super(MENSAGEM_PADRAO + " Detalhe: " + detalhe, HttpStatus.CONFLICT);
    }
}
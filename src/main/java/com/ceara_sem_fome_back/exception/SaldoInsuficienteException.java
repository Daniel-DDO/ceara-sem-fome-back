package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;
//status 402 PAYMENT REQUIRED
public class SaldoInsuficienteException extends NegocioException {
    
    private static final String MENSAGEM_PADRAO = "Saldo insuficiente para realizar a transação.";

    public SaldoInsuficienteException() {
        super(MENSAGEM_PADRAO, HttpStatus.PAYMENT_REQUIRED);
    }
    
    public SaldoInsuficienteException(String detalhe) {
        super(MENSAGEM_PADRAO + " Detalhe: " + detalhe, HttpStatus.PAYMENT_REQUIRED);
    }
}
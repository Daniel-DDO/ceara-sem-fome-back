package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends NegocioException {

    public SaldoInsuficienteException(BigDecimal saldoDisponivel, BigDecimal valorTotal) {
        super(formatarMensagem(saldoDisponivel, valorTotal), HttpStatus.CONFLICT);
    }

    private static String formatarMensagem(BigDecimal saldoDisponivel, BigDecimal valorTotal) {
        return String.format(
                "Saldo insuficiente para concluir a compra. Saldo disponível: R$ %.2f, Total da compra: R$ %.2f",
                saldoDisponivel,
                valorTotal
        );
    }

    private static final String MENSAGEM_PADRAO = "Saldo insuficiente para realizar a transação.";

    public SaldoInsuficienteException() {
        super(MENSAGEM_PADRAO, HttpStatus.PAYMENT_REQUIRED);
    }
    
    public SaldoInsuficienteException(String detalhe) {
        super(MENSAGEM_PADRAO + " Detalhe: " + detalhe, HttpStatus.PAYMENT_REQUIRED);
    }
}
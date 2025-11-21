package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;

public class CarrinhoVazioException extends NegocioException {

    // Define a mensagem padrão e o status HTTP como BAD_REQUEST (400)
    public CarrinhoVazioException() {
        super("O carrinho de compras está vazio.", HttpStatus.BAD_REQUEST);
    }

    public CarrinhoVazioException(String mensagem) {
        super(mensagem, HttpStatus.BAD_REQUEST);
    }
}

package com.ceara_sem_fome_back.exception;

public class ContaNaoExisteException extends RuntimeException {
    public ContaNaoExisteException(String message) {
        super("Verifique as credenciais da conta para fazer o login com: "+message);
    }
}

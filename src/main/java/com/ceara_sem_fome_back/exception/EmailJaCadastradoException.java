package com.ceara_sem_fome_back.exception;

public class EmailJaCadastradoException extends RecursoEmConflitoException {
    private static final String MENSAGEM_PADRAO = "O email informado já está cadastrado no sistema.";

    public EmailJaCadastradoException(String email) {
        super(MENSAGEM_PADRAO + " Email: " + email);
    }
}

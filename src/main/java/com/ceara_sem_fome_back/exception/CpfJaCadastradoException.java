package com.ceara_sem_fome_back.exception;

public class CpfJaCadastradoException extends RecursoEmConflitoException {
    private static final String MENSAGEM_PADRAO = "O CPF informado já está cadastrado no sistema.";

    public CpfJaCadastradoException(String cpf) {
        super(MENSAGEM_PADRAO + " CPF: " + cpf);
    }
}

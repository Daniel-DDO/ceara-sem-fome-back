package com.ceara_sem_fome_back.exception;


public class EstabelecimentoJaCadastradoException extends RecursoEmConflitoException {
    private static final String MENSAGEM_PADRAO = "Já existe um estabelecimento cadastrado com o ID informado.";

    public EstabelecimentoJaCadastradoException(String id) {
        super(MENSAGEM_PADRAO + " ID: " + id);
    }
}
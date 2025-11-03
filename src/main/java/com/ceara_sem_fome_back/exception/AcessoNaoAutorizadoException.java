package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;

public class AcessoNaoAutorizadoException extends NegocioException {
    private static final String MENSAGEM_PADRAO = "Você não possui acesso a esses dados.";
  public AcessoNaoAutorizadoException(String cpf) {
    super(MENSAGEM_PADRAO + " CPF: " + cpf, HttpStatus.FORBIDDEN);
  }
}

package com.ceara_sem_fome_back.exception;

import org.springframework.http.HttpStatus;

public class NegocioException extends RuntimeException {
  private final HttpStatus status;

  public NegocioException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}

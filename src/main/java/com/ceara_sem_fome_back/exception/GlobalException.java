package com.ceara_sem_fome_back.exception;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalException {

  @ExceptionHandler(ContaNaoExisteException.class)
  public ResponseEntity<String> tratarContaNaoExisteException(ContaNaoExisteException ex) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage()); //401
  }
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorDTO> handleNegocioException(NegocioException ex) {
        ErrorDTO errorDTO = new ErrorDTO(ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGeneralException(Exception ex) {
        ErrorDTO errorDTO = new ErrorDTO("Ocorreu um erro interno inesperado.", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
    }
}

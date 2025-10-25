package com.ceara_sem_fome_back.exception;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Erro de validação nos dados enviados.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", message
        ));
    }

    @ExceptionHandler(ContaNaoExisteException.class)
    public ResponseEntity<String> tratarContaNaoExisteException(ContaNaoExisteException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage()); //401
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorDTO> handleNegocioException(NegocioException ex) {
        ErrorDTO errorDTO = new ErrorDTO(ex.getMessage(), ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(LgpdNaoAceitaException.class)
    public ResponseEntity<?> handleLgpdNaoAceita(LgpdNaoAceitaException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGeneralException(Exception ex) {
        ErrorDTO errorDTO = new ErrorDTO("Ocorreu um erro interno inesperado.", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        ));
    }
}

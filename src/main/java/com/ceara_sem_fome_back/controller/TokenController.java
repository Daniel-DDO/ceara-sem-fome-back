package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.TokenResponse;
import com.ceara_sem_fome_back.service.CadastroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
public class TokenController {

    @Autowired
    private CadastroService cadastroService;

    @GetMapping("/confirmar-cadastro")
    public ResponseEntity<?> confirmarCadastro(@RequestParam String token) {
        boolean sucesso = cadastroService.verificarEFinalizarCadastro(token);

        if (sucesso) {
            return ResponseEntity.ok().body(new TokenResponse("sucesso", "Cadastro confirmado com sucesso."));
        } else {
            return ResponseEntity.badRequest().body(new TokenResponse("erro", "Token inválido ou expirado."));
        }
    }
}

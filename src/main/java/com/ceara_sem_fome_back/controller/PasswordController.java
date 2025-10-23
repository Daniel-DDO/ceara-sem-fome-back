package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.RecuperacaoSenhaDTO;
import com.ceara_sem_fome_back.dto.RedefinirSenhaFinalDTO;
import com.ceara_sem_fome_back.service.RecuperacaoSenhaService;
import com.ceara_sem_fome_back.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class PasswordController {

    @Autowired
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/iniciar-recuperacao")
    public ResponseEntity<String> iniciarRecuperacao(@RequestBody RecuperacaoSenhaDTO recuperacaoDTO) {
        recuperacaoSenhaService.iniciarRecuperacaoSenha(recuperacaoDTO);
        return ResponseEntity.ok("Se os dados estiverem corretos, um e-mail de recuperação foi enviado. Copie o token do link recebido.");
    }

    /**
     * [ESTÉTICA] Este método foi atualizado para gerar a página de status com o novo design.
     */
    @GetMapping("/validar-token-recuperacao")
    public ResponseEntity<String> validarTokenRecuperacao(@RequestParam String token) {
        boolean isTokenValid = tokenService.validarToken(token);

        String logoCearaSemFome = "https://www.ceara.gov.br/wp-content/uploads/2024/01/logo-cesf-e-cegov-e1704803051849-600x239.png";

        if (isTokenValid) {
            String htmlSuccess = String.format("""
                <!DOCTYPE html><html lang="pt-BR"><head><meta charset="UTF-8"><title>Token Válido</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: linear-gradient(to bottom, #E8F5E9, #F5F5F5); display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
                    .card { background-color: #fff; padding: 2rem; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.1); width: 100%; max-width: 500px; text-align: center; }
                    .header { display: flex; justify-content: center; align-items: center; gap: 1rem; border-bottom: 1px solid #eee; padding-bottom: 1rem; margin-bottom: 1.5rem; }
                    .header img { height: 40px; }
                    h1 { color: #333; font-size: 1.5rem; margin: 0 0 0.5rem 0; }
                    .success-text { color: #28a745; font-size: 1.2rem; font-weight: bold; margin: 0 0 1rem 0; }
                    p { color: #555; font-size: 1rem; margin: 0; }
                </style>
                </head><body>
                <div class="card">
                    <div class="header">
                        <img src="%s" alt="Ceará Sem Fome">
                    </div>
                    <h1>Verificação Concluída</h1>
                    <p class="success-text">O seu token foi verificado com sucesso.</p>
                    <p>Você já pode fechar esta aba e continuar o processo no Postman.</p>
                </div>
                </body></html>
                """, logoCearaSemFome);
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlSuccess);
        } else {
            String htmlError = String.format("""
                <!DOCTYPE html><html lang="pt-BR"><head><meta charset="UTF-8"><title>Token Inválido</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: linear-gradient(to bottom, #fde8e8, #F5F5F5); display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
                    .card { background-color: #fff; padding: 2rem; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.1); width: 100%; max-width: 500px; text-align: center; }
                    .header { display: flex; justify-content: center; align-items: center; gap: 1rem; border-bottom: 1px solid #eee; padding-bottom: 1rem; margin-bottom: 1.5rem; }
                    .header img { height: 40px; }
                    h1 { color: #333; font-size: 1.5rem; margin: 0 0 0.5rem 0; }
                    .error-text { color: #dc3545; font-size: 1.2rem; font-weight: bold; margin: 0 0 1rem 0; }
                    p { color: #555; font-size: 1rem; margin: 0; }
                </style>
                </head><body>
                <div class="card">
                    <div class="header">
                        <img src="%s" alt="Ceará Sem Fome">
                    </div>
                    <h1>Erro na Verificação</h1>
                    <p class="error-text">Token Inválido ou Expirado.</p>
                    <p>Por favor, solicite um novo link de recuperação.</p>
                </div>
                </body></html>
                """, logoCearaSemFome);
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_HTML).body(htmlError);
        }
    }

    @PostMapping("/redefinir-senha-final")
    public ResponseEntity<String> processResetPassword(@RequestBody RedefinirSenhaFinalDTO redefinirDTO) {
        boolean sucesso = recuperacaoSenhaService.redefinirSenha(redefinirDTO);

        if (sucesso) {
            return ResponseEntity.ok("Senha redefinida com sucesso!");
        } else {
            return ResponseEntity.badRequest().body("Falha ao redefinir a senha. O token pode ser inválido, as senhas podem não coincidir ou o link expirou.");
        }
    }
}


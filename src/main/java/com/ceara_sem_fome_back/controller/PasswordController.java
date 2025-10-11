package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.RecuperacaoSenhaDTO;
import com.ceara_sem_fome_back.dto.RedefinirSenhaFinalDTO; 
import com.ceara_sem_fome_back.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/auth")
public class PasswordController {

    @Autowired
    private TokenService tokenService;

    // Rota 1: Inicia a recuperação (recebe CPF+Email)
    @PostMapping("/recuperar")
    public ResponseEntity<String> iniciarRecuperacao(@RequestBody RecuperacaoSenhaDTO recuperacaoDTO) {
        boolean emailEnviado = tokenService.validateAndSendRecoveryEmail(recuperacaoDTO);

        if (emailEnviado) {
            return ResponseEntity.ok("Se o CPF e Email estiverem corretos, um link de verificação foi enviado.");
        } else {
            // Retorno de segurança: Evita informar se o erro foi o CPF ou o Email
            return ResponseEntity.badRequest().body("Não foi possível processar a recuperação de senha ou os dados não batem.");
        }
    }
    
    // Rota 2: O pedágio (o usuário clica no e-mail e valida o token)
    @GetMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token) {
        boolean isValid = tokenService.validateVerificationToken(token);
        
        if (isValid) {
            String htmlSuccess = 
                "<html><head><meta charset=\"UTF-8\"><title>Verificação Concluída</title></head><body style=\"font-family: Arial, sans-serif; text-align: center; margin-top: 50px;\">"
                + "<h1 style=\"color: #28a745;\">✅ Verificação Concluída!</h1>"
                + "<p style=\"font-size: 18px; color: #555;\">O seu e-mail foi verificado. Você j&aacute; pode fechar esta aba e continuar o processo de redefini&ccedil;&atilde;o de senha no aplicativo.</p>"
                + "</body></html>";
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlSuccess);
        } else {
            String htmlError = 
                "<html><head><meta charset=\"UTF-8\"><title>Erro na Verificação</title></head><body style=\"font-family: Arial, sans-serif; text-align: center; margin-top: 50px;\">"
                             + "<h1 style=\"color: #dc3545;\">❌ Token Inv&aacute;lido ou Expirado</h1>"
                             + "<p style=\"font-size: 18px; color: #555;\">O link de verifica&ccedil;&atilde;o n&atilde;o &eacute; mais v&aacute;lido. Por favor, solicite um novo link.</p>"
                             + "</body></html>";
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_HTML).body(htmlError);
        }
    }
    
    // Rota 3: Consumo Final do Token e Redefinição da Senha
    @PostMapping("/resetar-senha-final")
    public ResponseEntity<String> resetarSenhaFinal(@RequestBody RedefinirSenhaFinalDTO redefinirDTO) {
        boolean sucesso = tokenService.resetUserPassword(redefinirDTO);

        if (sucesso) {
            return ResponseEntity.ok("Senha redefinida com sucesso!");
        } else {
            // Este erro ocorre se as senhas não coincidirem ou se o token for inválido/inexistente.
            return ResponseEntity.badRequest().body("Falha ao redefinir a senha. As senhas não coincidem ou o token é inválido.");
        }
    }
}

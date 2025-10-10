package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.service.EmailService;
import com.ceara_sem_fome_back.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private TokenService tokenService; 

    @GetMapping("/send-verification")
    public ResponseEntity<String> sendVerificationEmail(@RequestParam String userEmail) {
        try {
            emailService.sendVerificationEmail(userEmail);
            return ResponseEntity.ok("E-mail de verificação enviado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Falha ao enviar o e-mail: " + e.getMessage());
        }
    }

    @GetMapping("/verify-token")
    public ResponseEntity<String> verifyToken(@RequestParam String token) {
        boolean isValid = tokenService.validateVerificationToken(token);

        if (isValid) {
            // Retorna uma página HTML de sucesso com a meta tag de UTF-8 corrigida
            String htmlSuccess = "<html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, sans-serif; text-align: center; margin-top: 50px;\">"
                               + "<h1 style=\"color: #28a745;\">✅ E-mail Verificado com Sucesso!</h1>"
                               + "<p style=\"font-size: 18px; color: #555;\">Sua conta foi verificada com sucesso. Você já pode fechar esta aba e continuar o processo.</p>"
                               + "</body></html>";
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlSuccess);
        } else {
            // Retorna uma página HTML de erro com a meta tag de UTF-8 corrigida
            String htmlError = "<html><head><meta charset=\"UTF-8\"></head><body style=\"font-family: Arial, sans-serif; text-align: center; margin-top: 50px;\">"
                             + "<h1 style=\"color: #dc3545;\">❌ Token Inválido ou Expirado</h1>"
                             + "<p style=\"font-size: 18px; color: #555;\">O link de verificação não é mais válido. Por favor, solicite um novo link no aplicativo.</p>"
                             + "</body></html>";
            return ResponseEntity.badRequest().contentType(MediaType.TEXT_HTML).body(htmlError);
        }
    }
}
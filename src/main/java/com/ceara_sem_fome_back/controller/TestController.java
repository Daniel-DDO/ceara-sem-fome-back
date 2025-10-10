package com.ceara_sem_fome_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ceara_sem_fome_back.service.EmailService;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        String subject = "Teste de Envio de E-mail - Projeto Ceará Sem Fome";
        String body = "Parabéns! Este é um e-mail de teste enviado com sucesso através do Spring Boot. Isso significa que suas configurações de e-mail estão corretas!";
        
        try {
            emailService.sendSimpleMail(to, subject, body);
            return ResponseEntity.ok("E-mail de teste enviado com sucesso para: " + to);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body("Falha ao enviar e-mail. Verifique o log e as configurações de e-mail. Detalhe: " + e.getMessage());
        }
    }
}
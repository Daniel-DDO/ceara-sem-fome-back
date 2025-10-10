package com.ceara_sem_fome_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por encapsular a lógica de envio de e-mails.
 * Utiliza o JavaMailSender configurado via application.properties.
 */
@Service
@Slf4j // Utiliza o logger do Lombok
public class EmailService {

    @Autowired
    private JavaMailSender mailSender; // Injeta a ferramenta de envio de e-mail do Spring

    /**
     * Envia um e-mail simples (apenas texto)
     * * @param to O destinatário do e-mail.
     * @param subject O assunto do e-mail.
     * @param body O corpo (conteúdo) do e-mail.
     */
    public void sendSimpleMail(String to, String subject, String body) {
        // 1. Cria o objeto da mensagem
        SimpleMailMessage message = new SimpleMailMessage();
        
        // O remetente é definido automaticamente pelo spring.mail.username no application.properties
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        // 2. Tenta enviar o e-mail
        try {
            mailSender.send(message);
            log.info("E-mail enviado com sucesso para: {}", to);
        } catch (MailException e) {
            // Se houver qualquer erro no envio (problemas com credenciais, host, etc.)
            log.error("Falha ao enviar e-mail para {}. Erro: {}", to, e.getMessage());
            // Você pode relançar uma exceção de serviço ou tratar o erro como preferir
            throw new RuntimeException("Não foi possível enviar o e-mail: " + e.getMessage());
        }
    }
}

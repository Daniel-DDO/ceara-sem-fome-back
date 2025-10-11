package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.VerificationToken;
import com.ceara_sem_fome_back.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    public void sendSimpleMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("E-mail de texto enviado com sucesso para: {}", to);
        } catch (MailException e) {
            log.error("Falha ao enviar e-mail de texto para {}. Erro: {}", to, e.getMessage());
            throw new RuntimeException("Não foi possível enviar o e-mail: " + e.getMessage());
        }
    }

    public void sendHtmlMail(String to, String subject, String htmlBody) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("E-mail HTML enviado com sucesso para: {}", to);
        } catch (MessagingException | MailException e) {
            log.error("Falha ao enviar e-mail HTML para {}. Erro: {}", to, e.getMessage());
            throw new RuntimeException("Não foi possível enviar o e-mail: " + e.getMessage());
        }
    }

    @Transactional
    public void sendVerificationEmail(String userEmail) {
        String token = UUID.randomUUID().toString();
        
        tokenRepository.deleteByUserEmail(userEmail);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserEmail(userEmail);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        tokenRepository.save(verificationToken);

        String verificationLink = "http://localhost:8080/test/verify-token?token=" + token;

        String emailBody = "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;\">"
            + "<table style=\"width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">"
            + "<tr><td style=\"padding: 20px; text-align: center;\">"
            + "<h1 style=\"color: #007bff;\">Confirmação de Acesso</h1>"
            + "<p style=\"color: #555555; font-size: 16px;\">Olá!</p>"
            + "<p style=\"color: #555555; font-size: 16px;\">Recebemos uma solicitação para verificar seu e-mail.</p>"
            + "<p style=\"color: #555555; font-size: 16px;\">Por favor, clique no botão abaixo para confirmar seu acesso.</p>"
            + "<p style=\"text-align: center; margin-top: 20px;\">"
            + "<a href=\"" + verificationLink + "\" style=\"display: inline-block; padding: 12px 24px; font-size: 16px; color: #ffffff; background-color: #007bff; text-decoration: none; border-radius: 5px;\">Verificar meu E-mail</a>"
            + "</p>"
            + "<p style=\"color: #999999; font-size: 14px; margin-top: 20px;\">Se você não solicitou esta verificação, por favor, ignore este e-mail.</p>"
            + "</td></tr></table></div>";

        this.sendHtmlMail(userEmail, "Verificação de E-mail", emailBody);

        log.info("E-mail de verificação enviado com sucesso para {}. Token criado.", userEmail);
    }
}
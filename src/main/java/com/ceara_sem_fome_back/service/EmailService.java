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

        String logoCearaSemFome = "https://www.ceara.gov.br/wp-content/uploads/2024/01/logo-cesf-e-cegov-e1704803051849-600x239.png";
        String logoGovernoCeara = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fe/Bras%C3%A3o_do_Cear%C3%A1.svg/500px-Bras%C3%A3o_do_Cear%C3%A1.svg.png";

        String emailBody = 
            "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f0f2f5; padding: 30px; margin: 0; line-height: 1.6;\">"
            + "<table role=\"presentation\" style=\"width: 100%; max-width: 600px; margin: 20px auto; border-spacing: 0; background-color: #ffffff; border-radius: 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.05); overflow: hidden;\">"
            
            // Header com Logos
            + "<tr><td style=\"background-color: #ffffff; padding: 25px 30px; text-align: left; border-bottom: 1px solid #e0e0e0;\">"
            + "<img src=\"" + logoCearaSemFome + "\" alt=\"Ceará sem Fome\" style=\"max-height: 45px; vertical-align: middle; margin-right: 20px;\">"
            + "<img src=\"" + logoGovernoCeara + "\" alt=\"Governo do Estado do Ceará\" style=\"max-height: 45px; vertical-align: middle;\">"
            + "</td></tr>"
            
            // Conteúdo principal
            + "<tr><td style=\"padding: 30px; text-align: center;\">"
            + "<h1 style=\"color: #333333; font-size: 28px; margin-bottom: 20px;\">Verificação de E-mail</h1>"
            + "<p style=\"color: #555555; font-size: 17px; margin-bottom: 25px;\">Olá!</p>"
            + "<p style=\"color: #555555; font-size: 17px; margin-bottom: 30px;\">Recebemos uma solicitação para verificar seu e-mail. Para continuar, por favor, clique no botão abaixo:</p>"
            
            // Botão
            + "<p style=\"text-align: center; margin-top: 20px; margin-bottom: 30px;\">"
            + "<a href=\"" + verificationLink + "\" style=\"display: inline-block; padding: 15px 30px; font-size: 18px; color: #ffffff; background-color: #1D9669; text-decoration: none; border-radius: 8px; font-weight: bold;\">Verificar meu E-mail</a>"
            + "</p>"
            
            + "<p style=\"color: #777777; font-size: 15px; margin-top: 30px;\">Este link é válido por 5 minutos.</p>"
            + "</td></tr>"
            
            // Rodapé
            + "<tr><td style=\"background-color: #f8f8f8; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;\">"
            + "<p style=\"color: #aaaaaa; font-size: 13px; margin: 0;\">Se você não solicitou esta verificação, por favor, ignore este e-mail ou entre em contato com o suporte.</p>"
            + "<p style=\"color: #aaaaaa; font-size: 13px; margin: 5px 0 0;\">&copy; " + LocalDateTime.now().getYear() + " Ceará Sem Fome. Todos os direitos reservados.</p>"
            + "</td></tr>"
            
            + "</table>"
            + "</div>";

        this.sendHtmlMail(userEmail, "Verificação de E-mail", emailBody);

        log.info("E-mail de verificação enviado com sucesso para {}. Token criado.", userEmail);
    }
}
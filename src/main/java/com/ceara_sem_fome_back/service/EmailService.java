package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.security.Deploy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
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

    public void sendVerificationEmail(String toEmail, String token) {
        String confirmationUrl = Deploy.getLinkServidor()+"/token/confirmar-cadastro?token=" + token;
        String subject = "Verificação de E-mail - Ceará Sem Fome";
        String body = buildEmailTemplate(subject, "Para completar o seu cadastro, por favor, clique no botão abaixo:", confirmationUrl, "Verificar meu E-mail", "Este link é válido por 10 minutos.");
        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Envia o e-mail de recuperação com o link a apontar para a rota de validação.
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        // Este URL aponta para o endpoint GET /validar-token-recuperacao
        String recoveryLink = Deploy.getLinkServidor()+"/auth/validar-token-recuperacao?token=" + token;
        String subject = "Recuperação de Senha - Ceará Sem Fome";
        String body = buildEmailTemplate(subject, "Recebemos uma solicitação para redefinir sua senha. Clique no botão abaixo para validar o seu token.", recoveryLink, "Validar Token de Recuperação", "Este link é válido por 10 minutos.");
        sendHtmlEmail(toEmail, subject, body);
    }

    private String buildEmailTemplate(String title, String mainText, String link, String buttonText, String footerText) {
        String logoCearaSemFome = "https://www.ceara.gov.br/wp-content/uploads/2024/01/logo-cesf-e-cegov-e1704803051849-600x239.png";
        int year = LocalDateTime.now().getYear();

        //Template de email
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: 'Segoe UI', sans-serif; background-color: #f0f2f5; padding: 20px; margin: 0;">
                <table role="presentation" style="width: 100%%; max-width: 600px; margin: 20px auto; border-spacing: 0; background-color: #ffffff; border-radius: 10px; box-shadow: 0 5px 15px rgba(0,0,0,0.05); overflow: hidden;">
                    <tr>
                        <td style="padding: 25px 30px; border-bottom: 1px solid #e0e0e0;">
                            <img src="%s" alt="Ceará sem Fome" style="max-height: 45px; vertical-align: middle;">
                        </td>
                    </tr>
                    <tr>
                        <td style="padding: 40px 30px; text-align: center;">
                            <h1 style="color: #333; font-size: 26px; margin-bottom: 20px;">%s</h1>
                            <p style="color: #555; font-size: 16px; margin-bottom: 30px;">%s</p>
                            <a href="%s" style="display: inline-block; padding: 14px 28px; font-size: 16px; color: #ffffff; background-color: #1D9669; text-decoration: none; border-radius: 8px; font-weight: bold;">%s</a>
                            <p style="color: #777; font-size: 14px; margin-top: 30px;">%s</p>
                        </td>
                    </tr>
                    <tr>
                        <td style="background-color: #f8f8f8; padding: 20px 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                            <p style="color: #aaa; font-size: 12px; margin: 0;">&copy; %d Ceará Sem Fome. Todos os direitos reservados.</p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, logoCearaSemFome, title, mainText, link, buttonText, footerText, year);
    }
}


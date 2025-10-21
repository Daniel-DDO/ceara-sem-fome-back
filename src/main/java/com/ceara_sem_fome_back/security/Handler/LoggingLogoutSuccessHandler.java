package com.ceara_sem_fome_back.security.Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component 
@Slf4j
public class LoggingLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (authentication != null) {
            String username = authentication.getName();
            String ipAddress = request.getRemoteAddr(); // Obtém o IP da requisição

            log.info(
                "SUCESSO LOGOUT: Usuário [{}] deslogou com sucesso. IP: {}",
                username,
                ipAddress
            );
        }

        // Continua com o comportamento padrão de logout (que no seu caso é apenas dar um 200 OK)
        super.onLogoutSuccess(request, response, authentication);
    }
}
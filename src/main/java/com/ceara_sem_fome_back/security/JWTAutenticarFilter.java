package com.ceara_sem_fome_back.security;

import com.ceara_sem_fome_back.dto.LoginDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Slf4j //ANOTAÇÃO DE LOG
public class JWTAutenticarFilter extends UsernamePasswordAuthenticationFilter {

    public static final int TOKEN_EXPIRACAO = 600_000; //10 minutos
    private final String tokenSenha;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JWTAutenticarFilter(AuthenticationManager authenticationManager, String tokenSenha) {
        this.authenticationManager = authenticationManager;
        this.tokenSenha = tokenSenha;
    }

    @Override
    public Authentication attemptAuthentication(@NonNull HttpServletRequest request,
                                                @NonNull HttpServletResponse response) throws AuthenticationException {
        
        LoginDTO login = null; //Declarar fora do try para usar no catch
        String ipAddress = request.getRemoteAddr(); //Captura IP

        try {
            //Lê como LoginDTO
            login = objectMapper.readValue(request.getInputStream(), LoginDTO.class);

            String email = login.getEmail();
            String senha = login.getSenha();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, senha, new ArrayList<>());

            return authenticationManager.authenticate(authToken);

        } catch (AuthenticationException e) {
            //LOG DE FALHA DE LOGIN (Ex.: Senha incorreta)
            String username = (login != null) ? login.getEmail() : "N/A";
            log.warn(
                "FALHA LOGIN: Tentativa falhou para o usuário [{}]. Motivo: {}. IP: {}",
                username,
                e.getMessage(),
                ipAddress
            );
            throw e; //Re-lança a exceção para o Spring tratar como falha

        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler dados de autenticação do request", e);
        }
    }

    @Override
    protected void successfulAuthentication(@NonNull HttpServletRequest request,
                                            @NonNull HttpServletResponse response,
                                            @NonNull FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        //Usa UserDetails para obter username
        Object principal = authResult.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal != null ? principal.toString() : null;
        }

        String ipAddress = request.getRemoteAddr(); //Captura IP

        //LOG DE SUCESSO DE LOGIN
        log.info(
            "SUCESSO LOGIN: Usuário [{}] logou com sucesso. IP: {}",
            username,
            ipAddress
        );

        String token = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRACAO))
                .sign(Algorithm.HMAC512(tokenSenha));

        //Retorna token no corpo e no header Authorization (padrão Bearer)
        response.setHeader("Authorization", "Bearer " + token);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = objectMapper.writeValueAsString(new TokenResponse("Bearer " + token));
        response.getWriter().write(json);
        response.getWriter().flush();
    }

    //Classe interna simples para resposta JSON
    private static class TokenResponse {
        public final String token;

        public TokenResponse(String token) {
            this.token = token;
        }
    }
}
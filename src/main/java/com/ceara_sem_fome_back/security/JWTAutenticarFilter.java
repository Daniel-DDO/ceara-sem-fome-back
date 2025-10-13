package com.ceara_sem_fome_back.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.model.Administrador;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class JWTAutenticarFilter extends UsernamePasswordAuthenticationFilter {

    public static final int TOKEN_EXPIRACAO = 600_000; // 10 minutos
    private final String tokenSenha; // agora é final
    private final AuthenticationManager authenticationManager;

    // construtor recebe AuthenticationManager e a senha do token
    public JWTAutenticarFilter(AuthenticationManager authenticationManager, String tokenSenha) {
        this.authenticationManager = authenticationManager;
        this.tokenSenha = tokenSenha;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            Administrador administrador = new ObjectMapper().readValue(request.getInputStream(), Administrador.class);

            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    administrador.getEmail(), administrador.getSenha(), new ArrayList<>()
            ));

        } catch (IOException e) {
            throw new RuntimeException("Falha ao autenticar usuário.", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        AdministradorData administradorData = (AdministradorData) authResult.getPrincipal();

        String token = JWT.create()
                .withSubject(administradorData.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRACAO))
                .sign(Algorithm.HMAC512(tokenSenha)); // usa a senha recebida no construtor

        response.getWriter().write(token);
        response.getWriter().flush();
    }
}

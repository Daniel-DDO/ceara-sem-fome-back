package com.ceara_sem_fome_back.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ceara_sem_fome_back.data.DetalheUsuarioData;
import com.ceara_sem_fome_back.model.Pessoa;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull; // [NOVO] Import necessário
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
    private final String tokenSenha;
    private final AuthenticationManager authenticationManager;

    public JWTAutenticarFilter(AuthenticationManager authenticationManager, String tokenSenha) {
        this.authenticationManager = authenticationManager;
        this.tokenSenha = tokenSenha;
    }

    @Override
    public Authentication attemptAuthentication(@NonNull HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            Pessoa pessoa = new ObjectMapper().readValue(request.getInputStream(), Pessoa.class);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    pessoa.getEmail(),
                    pessoa.getSenha(),
                    new ArrayList<>()
            ));
        } catch (IOException e) {
            throw new RuntimeException("Falha ao autenticar usuário", e);
        }
    }

    @Override
    protected void successfulAuthentication(@NonNull HttpServletRequest request,
                                            @NonNull HttpServletResponse response,
                                            @NonNull FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        DetalheUsuarioData usuarioData = (DetalheUsuarioData) authResult.getPrincipal();

        String token = JWT.create()
                .withSubject(usuarioData.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + TOKEN_EXPIRACAO))
                .sign(Algorithm.HMAC512(tokenSenha));

        response.getWriter().write(token);
        response.getWriter().flush();
    }
}

package com.ceara_sem_fome_back.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {

    public static final long TOKEN_EXPIRACAO = 1_800_000; //30 minutos

    @Value("${api.guid.token.senha}")
    private String tokenSenha;

    public String gerarToken(String email, long expiracao) {
        return JWT.create()
                .withSubject(email)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiracao))
                .sign(Algorithm.HMAC512(tokenSenha));
    }
}

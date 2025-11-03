package com.ceara_sem_fome_back.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.ceara_sem_fome_back.model.VerificationToken;
import com.ceara_sem_fome_back.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class TokenService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Value("${api.guid.token.senha}")
    private String jwtSecret;

    /**
     * Valida um token de forma genérica. Verifica se existe e não está expirado.
     * Esta é a única responsabilidade que restou nesta classe.
     * @param token O token a ser validado.
     * @return true se o token for válido, false caso contrário.
     */
    public boolean validarToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            log.warn("Tentativa de uso de token inexistente: {}", token);
            return false;
        }

        VerificationToken verificationToken = optionalToken.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Tentativa de uso de token expirado: {}", token);
            return false;
        }

        log.info("Token validado com sucesso (mas não consumido): {}", verificationToken.getUserEmail());
        return true;
    }

    public String validarTokenJWT(String token) throws JWTVerificationException {
        try {
            String email = JWT.require(Algorithm.HMAC512(jwtSecret))
                    .build()
                    .verify(token)
                    .getSubject();

            if (email == null) {
                throw new JWTVerificationException("Token válido, mas o 'subject' está nulo.");
            }

            return email;

        } catch (JWTVerificationException e) {
            log.warn("Falha ao validar JWT: {}", e.getMessage());
            throw e;
        }
    }
}


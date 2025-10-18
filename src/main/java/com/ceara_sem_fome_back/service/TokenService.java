package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.VerificationToken;
import com.ceara_sem_fome_back.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class TokenService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    /**
     * [SIMPLIFICADO] Valida um token de forma genérica. Verifica se existe e não está expirado.
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
}


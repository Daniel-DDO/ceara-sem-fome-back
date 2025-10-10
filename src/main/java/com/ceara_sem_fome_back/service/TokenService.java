package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.VerificationToken;
import com.ceara_sem_fome_back.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TokenService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    /**
     * Gera e salva um novo token de recuperação de senha com validade de 5 minutos.
     * @param userEmail O email do usuário que solicitou a recuperação.
     * @return O token gerado.
     */
    @Transactional
    public String createPasswordRecoveryToken(String userEmail) {
        // Gera um token único
        String token = UUID.randomUUID().toString();
        
        // **OPCIONAL:** Remove tokens antigos/pendentes do mesmo usuário (boa prática)
        // Isso garante que apenas o token mais recente seja válido
        tokenRepository.deleteByUserEmail(userEmail); 
        
        // Define a validade de 5 minutos
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserEmail(userEmail);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        
        tokenRepository.save(verificationToken);
        log.info("Token de recuperação de senha criado para {}. Expira em: {}", userEmail, verificationToken.getExpiryDate());
        
        return token;
    }

    /**
     * Valida se um token existe e se ainda está dentro do prazo de validade (5 minutos).
     * Se válido, o token é DELETADO imediatamente.
     * @param token O token recebido do link de e-mail.
     * @return True se o token for válido; False caso contrário.
     */
    @Transactional
    public boolean validateVerificationToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);
        
        if (optionalToken.isEmpty()) {
            log.warn("Tentativa de uso de token inexistente: {}", token);
            return false; // Token não encontrado
        }

        VerificationToken verificationToken = optionalToken.get();
        
        // 1. **VERIFICAÇÃO DE EXPIRAÇÃO CORRIGIDA:** // Checa se a data de expiração é ANTES do momento atual.
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            // Token expirou. Removemos do banco.
            tokenRepository.delete(verificationToken);
            log.warn("Token expirado deletado: {}", token);
            return false;
        }

        // 2. Se a data de expiração não passou (token é válido), consumimos e deletamos.
        tokenRepository.delete(verificationToken);
        log.info("Token validado e consumido com sucesso para email: {}", verificationToken.getUserEmail());
        
        return true;
    }
}

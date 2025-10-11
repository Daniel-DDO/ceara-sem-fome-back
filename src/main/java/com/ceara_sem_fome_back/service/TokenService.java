package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.RecuperacaoSenhaDTO;
import com.ceara_sem_fome_back.dto.RedefinirSenhaFinalDTO;
import com.ceara_sem_fome_back.model.VerificationToken;
import com.ceara_sem_fome_back.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class TokenService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;
    
    // FUTURO: Injete o repositório de usuários
    // @Autowired
    // private UsuarioRepository usuarioRepository; 


    /**
     * Etapa 1: Valida se CPF e Email batem (lógica de teste) e envia o e-mail.
     */
    public boolean validateAndSendRecoveryEmail(RecuperacaoSenhaDTO recuperacaoDTO) {
        final String TEST_EMAIL = "pedrorossiter@gmail.com";
        final String TEST_CPF = "12345678900"; 

        boolean credenciaisValidas = recuperacaoDTO.getEmail().equals(TEST_EMAIL) &&
                                     recuperacaoDTO.getCpf().equals(TEST_CPF);

        if (credenciaisValidas) {
            try {
                emailService.sendVerificationEmail(recuperacaoDTO.getEmail());
                return true;
            } catch (RuntimeException e) {
                log.error("Falha ao enviar e-mail após validação de CPF/Email: {}", e.getMessage());
                return false;
            }
        } else {
            log.warn("Tentativa de recuperação de senha falhou: CPF/Email incorretos para {}", recuperacaoDTO.getEmail());
            return false;
        }
    }


    /**
     * Etapa 2: Valida o token recebido no link. Não o consome aqui.
     */
    @Transactional
    public boolean validateVerificationToken(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);
        
        if (optionalToken.isEmpty()) {
             log.warn("Tentativa de uso de token inexistente: {}", token);
             return false;
         }
         VerificationToken verificationToken = optionalToken.get();
         if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
             tokenRepository.delete(verificationToken);
             log.warn("Token expirado deletado: {}", token);
             return false;
         }
         log.info("Token validado com sucesso (mas NÃO CONSUMIDO): {}", verificationToken.getUserEmail());
         return true;
    }
    
    /**
     * Etapa 3: Recebe o token e a senha, valida e consome o token.
     */
    @Transactional
    public boolean resetUserPassword(RedefinirSenhaFinalDTO redefinirDTO) {
        // 1. **VERIFICAÇÃO DE UX:** As senhas devem coincidir.
        if (!redefinirDTO.getNovaSenha().equals(redefinirDTO.getConfirmaNovaSenha())) {
            log.warn("Falha na redefinição de senha: As senhas não coincidem para o token {}", redefinirDTO.getToken());
            return false;
        }

        // 2. **SEGURANÇA:** O token é a prova de identidade. Verifica se ele existe.
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(redefinirDTO.getToken());
        if (optionalToken.isEmpty()) {
            log.warn("Tentativa de redefinição de senha com token inválido: {}", redefinirDTO.getToken());
            return false;
        }

        // 3. Obtém o e-mail do usuário a partir do token (chave para o banco de dados).
        String userEmail = optionalToken.get().getUserEmail(); 
        
        // 4. Lógica de Consumo do Token.
        tokenRepository.delete(optionalToken.get());
        
        log.info("SIMULANDO: Senha redefinida com sucesso para o email {}. Token utilizado: {}", userEmail, redefinirDTO.getToken());
        
        return true; 
    }
}
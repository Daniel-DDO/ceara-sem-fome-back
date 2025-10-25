package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.RecuperacaoSenhaDTO;
import com.ceara_sem_fome_back.dto.RedefinirSenhaFinalDTO;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RecuperacaoSenhaService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;
    @Autowired
    private AdministradorRepository administradorRepository;
    @Autowired
    private ComercianteRepository comercianteRepository;
    @Autowired
    private EntregadorRepository entregadorRepository;
    @Autowired
    private VerificationTokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Passo 1 do Fluxo: Recebe CPF e Email, valida se pertencem ao mesmo usuário e envia o e-mail.
     */
    @Transactional
    public void iniciarRecuperacaoSenha(RecuperacaoSenhaDTO recuperacaoDTO) {
        log.info("Iniciando verificação de credenciais para {}", recuperacaoDTO.getEmail());
        Optional<Beneficiario> beneficiario = beneficiarioRepository
                .findByCpfAndEmail(recuperacaoDTO.getCpf(), recuperacaoDTO.getEmail());
        Optional<Comerciante> comerciante = comercianteRepository
                .findByCpfAndEmail(recuperacaoDTO.getCpf(), recuperacaoDTO.getEmail());
        Optional<Entregador> entregador = entregadorRepository
                .findByCpfAndEmail(recuperacaoDTO.getCpf(), recuperacaoDTO.getEmail());
        Optional<Administrador> administrador = administradorRepository
                .findByCpfAndEmail(recuperacaoDTO.getCpf(), recuperacaoDTO.getEmail());

        if (beneficiario.isPresent()) {
            String userEmail = beneficiario.get().getEmail();
            enviarTokenRecuperacao(userEmail);
        } else if (comerciante.isPresent()) {
            String userEmail = comerciante.get().getEmail();
            enviarTokenRecuperacao(userEmail);
        } else if (entregador.isPresent()) {
            String userEmail = entregador.get().getEmail();
            enviarTokenRecuperacao(userEmail);
        } else if (administrador.isPresent()) {
            String userEmail = administrador.get().getEmail();
            enviarTokenRecuperacao(userEmail);
        } else {
            log.warn("Verificação falhou: CPF e/ou Email não correspondem a um usuário válido.");
        }
    }

    //deixando o metodo generico
    private void enviarTokenRecuperacao(String userEmail) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUserEmail(userEmail);
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10)); //validade de 10 minutos

        tokenRepository.save(verificationToken);

        emailService.sendPasswordResetEmail(userEmail, token);
        log.info("Verificação bem-sucedida. E-mail de recuperação enviado para {}", userEmail);
    }

    /**
     * Passo Final: Recebe os dados do formulário, valida o token e atualiza a senha.
     */
    @Transactional
    public boolean redefinirSenha(RedefinirSenhaFinalDTO redefinirDTO) {
        if (redefinirDTO.getNovaSenha() == null || !redefinirDTO.getNovaSenha().equals(redefinirDTO.getConfirmaNovaSenha())) {
            log.warn("Falha na redefinição: As senhas não coincidem.");
            return false;
        }

        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(redefinirDTO.getToken());
        
        if (optionalToken.isEmpty() || optionalToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Falha na redefinição: Token inválido ou expirado.");
            optionalToken.ifPresent(tokenRepository::delete); // Apaga o token se ele existiu mas expirou
            return false;
        }

        VerificationToken token = optionalToken.get();
        Optional<Beneficiario> optionalBeneficiario = beneficiarioRepository.findByEmail(token.getUserEmail());
        Optional<Comerciante> optionalComerciante = comercianteRepository.findByEmail(token.getUserEmail());
        Optional<Entregador> optionalEntregador = entregadorRepository.findByEmail(token.getUserEmail());
        Optional<Administrador> optionalAdministrador = administradorRepository.findByEmail(token.getUserEmail());

        if (optionalBeneficiario.isPresent()) {
            Beneficiario beneficiario = optionalBeneficiario.get();
            beneficiario.setSenha(passwordEncoder.encode(redefinirDTO.getNovaSenha()));
            beneficiarioRepository.save(beneficiario);
            return senhaRedefinidaReturn(tokenRepository, token);
        } else if (optionalComerciante.isPresent()) {
            Comerciante comerciante = optionalComerciante.get();
            comerciante.setSenha(passwordEncoder.encode(redefinirDTO.getNovaSenha()));
            comercianteRepository.save(comerciante);
            return senhaRedefinidaReturn(tokenRepository, token);
        } else if (optionalEntregador.isPresent()) {
            Entregador entregador = optionalEntregador.get();
            entregador.setSenha(passwordEncoder.encode(redefinirDTO.getNovaSenha()));
            entregadorRepository.save(entregador);
            return senhaRedefinidaReturn(tokenRepository, token);
        } else if (optionalAdministrador.isPresent()) {
            Administrador administrador = optionalAdministrador.get();
            administrador.setSenha(passwordEncoder.encode(redefinirDTO.getNovaSenha()));
            administradorRepository.save(administrador);
            return senhaRedefinidaReturn(tokenRepository, token);
        }

        log.error("Erro crítico. Token válido, mas e-mail não encontrado: {}", token.getUserEmail());
        return false;
    }

    private boolean senhaRedefinidaReturn(VerificationTokenRepository tokenRepository, VerificationToken token) {
        tokenRepository.delete(token);
        log.info("Senha redefinida com sucesso para {}.", token.getUserEmail());
        return true;
    }
}


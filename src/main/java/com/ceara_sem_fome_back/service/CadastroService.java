package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.*;
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
public class CadastroService {

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * Cria um token "rico" contendo todos os dados do pré-cadastro,
     * salva no banco e envia o e-mail de verificação.
     * @param request Os dados do beneficiário para o pré-cadastro.
     */
    @Transactional
    public void criarTokenDeCadastroEVenviarEmail(CadastroRequest request, TipoPessoa tipoPessoa) {
        String tokenString = UUID.randomUUID().toString();
        //Limpa tokens antigos do mesmo e-mail para evitar lixo no banco
        tokenRepository.deleteByUserEmail(request.getEmail());

        VerificationToken verificationToken = new VerificationToken(
                tokenString,
                request.getNome(),
                request.getCpf(),
                request.getEmail(),
                passwordEncoder.encode(request.getSenha()),
                request.getDataNascimento(),
                request.getTelefone(),
                request.getGenero(),
                tipoPessoa
        );

        tokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(request.getEmail(), tokenString);
        log.info("Token de cadastro criado e e-mail enviado para {}", request.getEmail());
    }

    @Transactional
    public void criarTokenDeCadastroEVenviarEmailAdm(AdministradorRequest request) {
        criarTokenDeCadastroEVenviarEmail(request, TipoPessoa.ADMINISTRADOR);
    }

    @Transactional
    public void criarTokenDeCadastroEVenviarEmailBenef(BeneficiarioRequest request) {
        criarTokenDeCadastroEVenviarEmail(request, TipoPessoa.BENEFICIARIO);
    }

    @Transactional
    public void criarTokenDeCadastroEVenviarEmailComerc(ComercianteRequest request) {
        criarTokenDeCadastroEVenviarEmail(request, TipoPessoa.COMERCIANTE);
    }

    @Transactional
    public void criarTokenDeCadastroEVenviarEmailEntreg(EntregadorRequest request) {
        criarTokenDeCadastroEVenviarEmail(request, TipoPessoa.ENTREGADOR);
    }

    /**
     * Valida o token recebido do e-mail, cria o beneficiário no banco de dados
     * e apaga o token para que não seja reutilizado.
     * @param token O token da URL.
     * @return true se o cadastro foi concluído com sucesso.
     */
    @Transactional
    public boolean verificarEFinalizarCadastro(String token) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            log.warn("Tentativa de finalizar cadastro com token inexistente: {}", token);
            return false;
        }

        VerificationToken verificationToken = optionalToken.get();
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            log.warn("Token de cadastro expirado e deletado: {}", token);
            return false;
        }

        switch (verificationToken.getTipoPessoa()) {
            case ADMINISTRADOR -> {
                Administrador novoAdministrador = new Administrador(
                        verificationToken.getNome(),
                        verificationToken.getCpf(),
                        verificationToken.getUserEmail(),
                        verificationToken.getSenhaCriptografada(),
                        verificationToken.getDataNascimento(),
                        verificationToken.getTelefone(),
                        verificationToken.getGenero()
                );
                administradorRepository.save(novoAdministrador);
                tokenRepository.delete(verificationToken);

                log.info("SUCESSO: Administrador salvo após validação de e-mail: {}", novoAdministrador.getEmail());
                return true;
            }
            case BENEFICIARIO -> {
                Beneficiario novoBeneficiario = new Beneficiario(
                        verificationToken.getNome(),
                        verificationToken.getCpf(),
                        verificationToken.getUserEmail(),
                        verificationToken.getSenhaCriptografada(),
                        verificationToken.getDataNascimento(),
                        verificationToken.getTelefone(),
                        verificationToken.getGenero()
                );
                beneficiarioRepository.save(novoBeneficiario);
                tokenRepository.delete(verificationToken);

                log.info("SUCESSO: Beneficiário salvo após validação de e-mail: {}", novoBeneficiario.getEmail());
                return true;
            }
            case COMERCIANTE -> {
                Comerciante novoComerciante = new Comerciante(
                        verificationToken.getNome(),
                        verificationToken.getCpf(),
                        verificationToken.getUserEmail(),
                        verificationToken.getSenhaCriptografada(),
                        verificationToken.getDataNascimento(),
                        verificationToken.getTelefone(),
                        verificationToken.getGenero()
                );
                comercianteRepository.save(novoComerciante);
                tokenRepository.delete(verificationToken);

                log.info("SUCESSO: Comerciante salvo após validação de e-mail: {}", novoComerciante.getEmail());
                return true;
            }
            case ENTREGADOR -> {
                Entregador novoEntregador = new Entregador(
                        verificationToken.getNome(),
                        verificationToken.getCpf(),
                        verificationToken.getUserEmail(),
                        verificationToken.getSenhaCriptografada(),
                        verificationToken.getDataNascimento(),
                        verificationToken.getTelefone(),
                        verificationToken.getGenero()
                );
                entregadorRepository.save(novoEntregador);
                tokenRepository.delete(verificationToken);

                log.info("SUCESSO: Entregador salvo após validação de e-mail: {}", novoEntregador.getEmail());
                return true;
            }
        }
        return false;
    }
}


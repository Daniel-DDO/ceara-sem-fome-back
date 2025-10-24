package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.*;
// IMPORTS NOVOS DAS SUAS EXCEÇÕES
import com.ceara_sem_fome_back.exception.CpfJaCadastradoException;
import com.ceara_sem_fome_back.exception.EmailJaCadastradoException;
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

    // MÉTODOS DE VALIDAÇÃO NOVOS (USANDO SUAS EXCEÇÕES)

    /**
     * Verifica se o CPF está disponível em TODAS as tabelas de usuários.
     * Lança uma CpfJaCadastradoException se o CPF já estiver em uso.
     */
    public void validarCpfDisponivelEmTodosOsPerfis(String cpf) {
        if (beneficiarioRepository.existsByCpf(cpf) ||
            administradorRepository.existsByCpf(cpf) ||
            comercianteRepository.existsByCpf(cpf) ||
            entregadorRepository.existsByCpf(cpf)) {
            
            // Lança sua exceção customizada
            throw new CpfJaCadastradoException(cpf);
        }
    }

    /**
     * Verifica se o Email está disponível em TODAS as tabelas de usuários.
     * Lança uma EmailJaCadastradoException se o Email já estiver em uso.
     */
    public void validarEmailDisponivelEmTodosOsPerfis(String email) {
        if (beneficiarioRepository.existsByEmail(email) ||
            administradorRepository.existsByEmail(email) ||
            comercianteRepository.existsByEmail(email) ||
            entregadorRepository.existsByEmail(email)) {

            // Lança sua exceção customizada
            throw new EmailJaCadastradoException(email);
        }
    }

    // FIM DOS MÉTODOS DE VALIDAÇÃO

    /**
     * Cria um token "rico" contendo todos os dados do pré-cadastro,
     * salva no banco e envia o e-mail de verificação.
     * @param request Os dados do beneficiário para o pré-cadastro.
     */
    @Transactional
    public void criarTokenDeCadastroEVenviarEmail(CadastroRequest request, TipoPessoa tipoPessoa) {
        
        // 🛡️ PASSO DE VALIDAÇÃO ADICIONADO AQUI 🛡️
        // Verifica ANTES de criar o token se os dados já existem
        validarCpfDisponivelEmTodosOsPerfis(request.getCpf());
        validarEmailDisponivelEmTodosOsPerfis(request.getEmail());

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

        VerificationToken saved = tokenRepository.save(verificationToken);
        log.info("Token salvo no banco: {}", saved.getToken());
        emailService.sendVerificationEmail(request.getEmail(), tokenString);
        log.info("Token de cadastro criado e e-mail enviado para {}", request.getEmail());
    }

    // ... (Seus métodos criarTokenDeCadastroEVenviarEmailAdm, Benef, Comerc, Entreg) ...
    // ELES NÃO MUDAM
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

        // 🛡️ VALIDAÇÃO BÔNUS (Evita Race Condition) 🛡️
        // Verifica de novo ANTES de salvar, caso alguém tenha se cadastrado
        // com o mesmo CPF/Email enquanto este token estava ativo.
        try {
            validarCpfDisponivelEmTodosOsPerfis(verificationToken.getCpf());
            validarEmailDisponivelEmTodosOsPerfis(verificationToken.getUserEmail());
        } catch (CpfJaCadastradoException | EmailJaCadastradoException e) {
            log.warn("Cadastro bloqueado na finalização (race condition): {}", e.getMessage());
            tokenRepository.delete(verificationToken); // Limpa o token inválido
            return false; // Falha na verificação, segue o padrão do método
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
            }
            default -> {
                log.error("Tipo de pessoa desconhecido no token: {}", verificationToken.getTipoPessoa());
                tokenRepository.delete(verificationToken);
                return false;
            }
        }
        
        // Se chegou até aqui, o usuário foi salvo com sucesso.
        // Apagamos o token e retornamos true.
        tokenRepository.delete(verificationToken);
        log.info("SUCESSO: {} salvo após validação de e-mail: {}", verificationToken.getTipoPessoa(), verificationToken.getUserEmail());
        return true;
    }
}
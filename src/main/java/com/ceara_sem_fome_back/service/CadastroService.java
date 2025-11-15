package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.exception.CpfJaCadastradoException;
import com.ceara_sem_fome_back.exception.EmailJaCadastradoException;
import com.ceara_sem_fome_back.exception.LgpdNaoAceitaException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // <<< IMPORTAR
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

    // --- CORREÇÃO DA DEPENDÊNCIA CIRCULAR ---
    @Autowired
    @Lazy // Adiciona esta anotação
    private PasswordEncoder passwordEncoder;
    // --- FIM DA CORREÇÃO ---

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificacaoService notificacaoService;

    //MÉTODOS DE VALIDAÇÃO

    /**
     * Verifica se o CPF está disponível em TODAS as tabelas de usuários.
     * Lança uma CpfJaCadastradoException se o CPF já estiver em uso.
     */
    public void validarCpfDisponivelEmTodosOsPerfis(String cpf) {
        if (beneficiarioRepository.existsByCpf(cpf) ||
            administradorRepository.existsByCpf(cpf) ||
            comercianteRepository.existsByCpf(cpf) ||
            entregadorRepository.existsByCpf(cpf)) {

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

            throw new EmailJaCadastradoException(email);
        }
    }

    //FIM DOS MÉTODOS DE VALIDAÇÃO

    /**
     * Cria um token "rico" contendo todos os dados do pré-cadastro,
     * salva no banco e envia o e-mail de verificação.
     * @param request Os dados do beneficiário para o pré-cadastro.
     */
    @Transactional
    public void criarTokenDeCadastroEVenviarEmail(CadastroRequest request, TipoPessoa tipoPessoa) {

        if ((Boolean.FALSE.equals(request.getLgpdAccepted()))) {
            log.info("LGPD não foi aceita. Aceite e tente cadastrar novamente.");
            throw new LgpdNaoAceitaException("Você deve aceitar os termos da LGPD para se cadastrar.");
        }

        //Verifica ANTES de criar o token se os dados já existem
        validarCpfDisponivelEmTodosOsPerfis(request.getCpf());
        validarEmailDisponivelEmTodosOsPerfis(request.getEmail());

        //Limpa tokens antigos do mesmo e-mail para evitar lixo no banco
        tokenRepository.deleteByUserEmail(request.getEmail());

        String tokenString = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken(
                tokenString,
                request.getNome(),
                request.getCpf(),
                request.getEmail(),
                passwordEncoder.encode(request.getSenha()),
                request.getDataNascimento(),
                request.getTelefone(),
                request.getGenero(),
                tipoPessoa,
                request.getLgpdAccepted()
        );

        VerificationToken saved = tokenRepository.save(verificationToken);

        log.info("Token salvo no banco: {}", saved.getToken());
        emailService.sendVerificationEmail(request.getEmail(), tokenString);
        log.info("E-mail de verificação enviado para {}", request.getEmail());
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

        //(Evita race condition)
        try {
            validarCpfDisponivelEmTodosOsPerfis(verificationToken.getCpf());
            validarEmailDisponivelEmTodosOsPerfis(verificationToken.getUserEmail());
        } catch (CpfJaCadastradoException | EmailJaCadastradoException e) {
            log.warn("Cadastro bloqueado na finalização (race condition): {}", e.getMessage());
            tokenRepository.delete(verificationToken); //Limpa o token inválido
            return false; //Falha na verificação, segue o padrão do metodo
        }
        
        String novoUsuarioId = null;
        String nomeUsuario = verificationToken.getNome().split(" ")[0];

        switch (verificationToken.getTipoPessoa()) {
            case ADMINISTRADOR -> {
                Administrador novoAdministrador = new Administrador(
                        verificationToken.getNome(),
                        verificationToken.getCpf(),
                        verificationToken.getUserEmail(),
                        verificationToken.getSenhaCriptografada(),
                        verificationToken.getDataNascimento(),
                        verificationToken.getTelefone(),
                        verificationToken.getGenero(),
                        verificationToken.getLgpdAccepted()
                );
                Administrador salvo = administradorRepository.save(novoAdministrador);
                novoUsuarioId = salvo.getId();
            }
            case BENEFICIARIO -> {
                Beneficiario novoBeneficiario = new Beneficiario(
                        verificationToken.getNome(),
                        verificationToken.getCpf(),
                        verificationToken.getUserEmail(),
                        verificationToken.getSenhaCriptografada(),
                        verificationToken.getDataNascimento(),
                        verificationToken.getTelefone(),
                        verificationToken.getGenero(),
                        verificationToken.getLgpdAccepted()
                );
                Beneficiario salvo = beneficiarioRepository.save(novoBeneficiario);
                novoUsuarioId = salvo.getId();
            }
            case COMERCIANTE -> {
                Comerciante novoComerciante = new Comerciante(
                        verificationToken.getNome(),
                        verificationToken.getCpf(),
                        verificationToken.getUserEmail(),
                        verificationToken.getSenhaCriptografada(),
                        verificationToken.getDataNascimento(),
                        verificationToken.getTelefone(),
                        verificationToken.getGenero(),
                        verificationToken.getLgpdAccepted()
                );
                Comerciante salvo = comercianteRepository.save(novoComerciante);
                novoUsuarioId = salvo.getId();
            }
            case ENTREGADOR -> {
                Entregador novoEntregador = new Entregador(
                        verificationToken.getNome(),
                        verificationToken.getCpf(),
                        verificationToken.getUserEmail(),
                        verificationToken.getSenhaCriptografada(),
                        verificationToken.getDataNascimento(),
                        verificationToken.getTelefone(),
                        verificationToken.getGenero(),
                        verificationToken.getLgpdAccepted()
                );
                Entregador salvo = entregadorRepository.save(novoEntregador);
                novoUsuarioId = salvo.getId();
            }
            default -> {
                log.error("Tipo de pessoa desconhecido no token: {}", verificationToken.getTipoPessoa());
                tokenRepository.delete(verificationToken);
                return false;
            }
        }
        
        //Se chegou até aqui, o usuário foi salvo com sucesso.
        //o token é apagado e o retorno é true.
        tokenRepository.delete(verificationToken);

        if (novoUsuarioId != null) {
            String mensagemBoasVindas = String.format(
                    "Seja bem-vindo(a), %s! Seu cadastro foi confirmado com sucesso.",
                    nomeUsuario
            );
            
            notificacaoService.criarNotificacao(novoUsuarioId, mensagemBoasVindas);
        }

        log.info("SUCESSO: {} salvo após validação de e-mail: {}", verificationToken.getTipoPessoa(), verificationToken.getUserEmail());
        return true;
    }

    //METODO DE REATIVAÇÃO

    /**
     * Reativa uma conta (reverte soft delete) de qualquer tipo de Pessoa.
     * Busca o usuário em todos os repositórios, ignorando o status.
     *
     * @param userId O ID da pessoa
     * @return A Pessoa que foi reativada.
     */
    @Transactional
    public Pessoa reativarConta(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo.");
        }

        // Tenta encontrar em qualquer um dos repositórios usando o bypass
        Optional<Beneficiario> ben = beneficiarioRepository.findByIdIgnoringStatus(userId);
        if (ben.isPresent()) {
            return reativarPessoa(ben.get());
        }

        Optional<Comerciante> com = comercianteRepository.findByIdIgnoringStatus(userId);
        if (com.isPresent()) {
            return reativarPessoa(com.get());
        }

        Optional<Entregador> ent = entregadorRepository.findByIdIgnoringStatus(userId);
        if (ent.isPresent()) {
            return reativarPessoa(ent.get());
        }

        Optional<Administrador> adm = administradorRepository.findByIdIgnoringStatus(userId);
        if (adm.isPresent()) {
            return reativarPessoa(adm.get());
        }

        // Se não encontrou em nenhum
        log.warn("Tentativa de reativar conta falhou. ID não encontrado: {}", userId);
        throw new RecursoNaoEncontradoException("Usuário não encontrado com o ID: " + userId);
    }

    /**
     * Metodo helper privado para setar o status e salvar
     */
    private Pessoa reativarPessoa(Pessoa pessoa) {
        if (pessoa.getStatus() == StatusPessoa.ATIVO) {
            log.info("Conta {} já estava ATIVA. Nenhuma alteração feita.", pessoa.getId());
            return pessoa; // Já está ativa
        }

        pessoa.setStatus(StatusPessoa.ATIVO);

        // Salva no repositório específico
        if (pessoa instanceof Beneficiario b) beneficiarioRepository.save(b);
        else if (pessoa instanceof Comerciante c) comercianteRepository.save(c);
        else if (pessoa instanceof Entregador e) entregadorRepository.save(e);
        else if (pessoa instanceof Administrador a) administradorRepository.save(a);

        log.info("SUCESSO: Conta {} ({}) reativada.", pessoa.getId(), pessoa.getClass().getSimpleName());
        return pessoa;
    }
}
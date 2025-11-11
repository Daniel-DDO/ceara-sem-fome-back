package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.AdministradorRepository;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
import com.ceara_sem_fome_back.repository.EntregadorRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PessoaService {

    private final AdministradorRepository administradorRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final ComercianteRepository comercianteRepository;
    private final EntregadorRepository entregadorRepository;

    public PessoaService(AdministradorRepository administradorRepository,
                         BeneficiarioRepository beneficiarioRepository,
                         ComercianteRepository comercianteRepository,
                         EntregadorRepository entregadorRepository) {
        this.administradorRepository = administradorRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.comercianteRepository = comercianteRepository;
        this.entregadorRepository = entregadorRepository;
    }

    /**
     * Obtém a entidade Pessoa associada ao usuário atualmente autenticado.
     */
    public Pessoa getUsuarioLogado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não autenticado.");
        }
        String email = authentication.getName();
        return findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
    }

    /**
     * Procura uma pessoa em todos os repositórios relevantes pelo email.
     */
    public Optional<Pessoa> findByEmail(String email) {
        Optional<Administrador> admin = administradorRepository.findByEmail(email);
        if (admin.isPresent()) {
            return Optional.of(admin.get());
        }

        Optional<Beneficiario> bene = beneficiarioRepository.findByEmail(email);
        if (bene.isPresent()) {
            return Optional.of(bene.get());
        }

        Optional<Comerciante> comer = comercianteRepository.findByEmail(email);
        if (comer.isPresent()) {
            return Optional.of(comer.get());
        }

        Optional<Entregador> entr = entregadorRepository.findByEmail(email);
        if (entr.isPresent()) {
            return Optional.of(entr.get());
        }

        return Optional.empty();
    }

    public Pessoa desativarConta(String id) {
        Pessoa pessoa = buscarPessoaPorId(id);

        if (!(pessoa instanceof Beneficiario ||
              pessoa instanceof Comerciante ||
              pessoa instanceof Entregador)) {
            throw new IllegalArgumentException("Tipo de pessoa inválido para desativação de conta.");
        }

        pessoa.setStatus(StatusPessoa.INATIVO);
        salvarPessoa(pessoa);
        return pessoa;
    }

    public Pessoa ativarConta(String id) {
        Pessoa pessoa = buscarPessoaPorId(id);

        if (!(pessoa instanceof Beneficiario ||
              pessoa instanceof Comerciante ||
              pessoa instanceof Entregador)) {
            throw new IllegalArgumentException("Tipo de pessoa inválido para ativação de conta.");
        }

        pessoa.setStatus(StatusPessoa.ATIVO);
        salvarPessoa(pessoa);
        return pessoa;
    }

    private Pessoa buscarPessoaPorId(String id) {
        Optional<? extends Pessoa> pessoaOpt;

        pessoaOpt = beneficiarioRepository.findById(id);
        if (pessoaOpt.isPresent()) return pessoaOpt.get();

        pessoaOpt = comercianteRepository.findById(id);
        if (pessoaOpt.isPresent()) return pessoaOpt.get();

        pessoaOpt = entregadorRepository.findById(id);
        if (pessoaOpt.isPresent()) return pessoaOpt.get();

        throw new IllegalArgumentException("Pessoa com ID " + id + " não encontrada.");
    }

    private void salvarPessoa(Pessoa pessoa) {
        if (pessoa instanceof Beneficiario) {
            beneficiarioRepository.save((Beneficiario) pessoa);
        } else if (pessoa instanceof Comerciante) {
            comercianteRepository.save((Comerciante) pessoa);
        } else if (pessoa instanceof Entregador) {
            entregadorRepository.save((Entregador) pessoa);
        } else {
            throw new IllegalArgumentException("Tipo de pessoa inválido para salvar.");
        }
    }
}

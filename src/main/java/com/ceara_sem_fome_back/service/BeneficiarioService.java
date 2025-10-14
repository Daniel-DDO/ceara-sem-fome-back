package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.BeneficiarioRequest;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BeneficiarioService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // [MODIFICADO] Agora injeta o CadastroService para delegar a criação da conta
    @Autowired
    private CadastroService cadastroService;

    public Beneficiario logarBeneficiario(String email, String senha) {
        Optional<Beneficiario> optionalBeneficiario = beneficiarioRepository.findByEmail(email);

        if (optionalBeneficiario.isPresent()) {
            Beneficiario beneficiario = optionalBeneficiario.get();
            if (passwordEncoder.matches(senha, beneficiario.getSenha())) {
                return beneficiario;
            }
        }
        throw new ContaNaoExisteException(email);
    }

    // [MODIFICADO] A lógica complexa foi movida. Este método agora apenas valida e delega.
    @Transactional
    public void iniciarCadastro(BeneficiarioRequest request) {
        checkIfUserExists(request.getCpf(), request.getEmail());
        cadastroService.criarTokenDeCadastroEVenviarEmail(request);
    }

    private void checkIfUserExists(String cpf, String email) {
        if (beneficiarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("O e-mail informado já está cadastrado.");
        }
        if (beneficiarioRepository.findByCpf(cpf).isPresent()) {
            throw new IllegalArgumentException("O CPF informado já está cadastrado.");
        }
    }
}


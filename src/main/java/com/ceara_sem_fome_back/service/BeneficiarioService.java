package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BeneficiarioService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    public Beneficiario logarBeneficiario(String email, String senha) {
        Optional<Beneficiario> beneficiario = beneficiarioRepository.findByEmail(email);

        if (beneficiario.isPresent() && beneficiario.get().getSenha().equals(senha)) {
            return beneficiario.get();
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }

    public Beneficiario salvarBeneficiario(Beneficiario beneficiario) {
        if (beneficiarioRepository.existsById(beneficiario.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        if (beneficiarioRepository.findByEmail(beneficiario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        return beneficiarioRepository.save(beneficiario);
    }
}

package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.utils.PessoaUtils; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional; 

@Service
public class BeneficiarioService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    public Beneficiario logarBeneficiario(String email, String senha) {
        Optional<Beneficiario> optionalBeneficiario = beneficiarioRepository.findByEmail(email);

        if (optionalBeneficiario.isPresent()) {
            Beneficiario beneficiario = optionalBeneficiario.get();
            // LÓGICA SEM CRIPTOGRAFIA: Compara a senha digitada com a senha do banco diretamente
            if (beneficiario != null && beneficiario.getSenha().equals(senha)) {
                 return beneficiario;
            }
        }
        
        throw new ContaNaoExisteException(email);
    }
    
    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }
}
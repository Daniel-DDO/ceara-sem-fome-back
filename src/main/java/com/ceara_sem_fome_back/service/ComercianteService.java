package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComercianteService {

    @Autowired
    private ComercianteRepository comercianteRepository;

    public Comerciante logarComerciante(String email, String senha) {
        Comerciante comerciante = comercianteRepository.findByEmail(email);

        if (comerciante != null && comerciante.getSenha().equals(senha)) {
            return comerciante;
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }
}

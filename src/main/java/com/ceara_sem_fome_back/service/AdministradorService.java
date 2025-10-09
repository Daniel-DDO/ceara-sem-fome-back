package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    public Administrador logarAdm(String email, String senha) {
        Administrador administrador = administradorRepository.findByEmail(email);

        if (administrador != null && administrador.getSenha().equals(senha)) {
            return administrador;
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }
}

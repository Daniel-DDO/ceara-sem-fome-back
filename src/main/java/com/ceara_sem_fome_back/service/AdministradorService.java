package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdministradorService implements UserDetailsService {

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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Administrador administrador = administradorRepository.findByEmail(email);
        if (administrador == null) {
            throw new UsernameNotFoundException("Usuário com email "+email+" não encontrado.");
        }
        return new AdministradorData(Optional.of(administrador));
    }

}

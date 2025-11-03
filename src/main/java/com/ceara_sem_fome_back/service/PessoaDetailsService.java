package com.ceara_sem_fome_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PessoaDetailsService implements UserDetailsService {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private ComercianteService comercianteService;

    @Autowired
    private EntregadorService entregadorService;

    @Autowired
    private AdministradorService administradorService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return beneficiarioService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ignored) {}
        try {
            return comercianteService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ignored) {}
        try {
            return entregadorService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ignored) {}
        try {
            return administradorService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ignored) {}
        throw new UsernameNotFoundException("Usuário com email " + username + " não encontrado.");
    }
}

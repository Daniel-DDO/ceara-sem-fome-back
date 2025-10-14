package com.ceara_sem_fome_back.data;

import com.ceara_sem_fome_back.model.Pessoa;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class DetalheUsuarioData implements UserDetails {

    private final Optional<Pessoa> pessoa;

    public DetalheUsuarioData(Optional<Pessoa> pessoa) {
        this.pessoa = pessoa;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        // [CORRIGIDO] Extrai a senha de forma segura, retornando null se a pessoa não existir
        return pessoa.map(Pessoa::getSenha).orElse(null);
    }

    @Override
    public String getUsername() {
        // [CORRIGIDO] Extrai o e-mail de forma segura, retornando null se a pessoa não existir
        return pessoa.map(Pessoa::getEmail).orElse(null);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // [ADICIONADO] Este era o método em falta.
        // Por agora, assumimos que se um utilizador existe, ele está ativo.
        return true;
    }
}


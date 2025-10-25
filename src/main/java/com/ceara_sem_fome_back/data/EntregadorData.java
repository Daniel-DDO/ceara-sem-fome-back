package com.ceara_sem_fome_back.data;

import com.ceara_sem_fome_back.model.Entregador;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Component
public class EntregadorData implements UserDetails {

    private final Optional<Entregador> entregador;

    public EntregadorData(Optional<Entregador> entregador) {
        this.entregador = entregador;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return entregador.orElse(new Entregador()).getSenha();
    }

    @Override
    public String getUsername() {
        return entregador.orElse(new Entregador()).getEmail();
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
        return true;
    }
}

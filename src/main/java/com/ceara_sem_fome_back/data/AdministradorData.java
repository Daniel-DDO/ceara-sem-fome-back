package com.ceara_sem_fome_back.data;

import com.ceara_sem_fome_back.model.Administrador;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Component
public class AdministradorData implements UserDetails {

    private final Optional<Administrador> administrador;

    public AdministradorData(Optional<Administrador> administrador) {
        this.administrador = administrador;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return administrador.orElse(new Administrador()).getSenha();
    }

    @Override
    public String getUsername() {
        return administrador.orElse(new Administrador()).getEmail();
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

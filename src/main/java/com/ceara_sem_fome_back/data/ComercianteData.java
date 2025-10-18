package com.ceara_sem_fome_back.data;

import com.ceara_sem_fome_back.model.Comerciante;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
//import java.util.List;
import java.util.Optional;

@Component
public class ComercianteData implements UserDetails {

    private final Optional<Comerciante> comerciante;

    public ComercianteData(Optional<Comerciante> comerciante) {
        this.comerciante = comerciante;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return comerciante.orElse(new Comerciante()).getSenha();
    }

    @Override
    public String getUsername() {
        return comerciante.orElse(new Comerciante()).getEmail();
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

package com.ceara_sem_fome_back.data;

import com.ceara_sem_fome_back.model.Beneficiario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Component
@Getter
public class BeneficiarioData implements UserDetails {

    private final Optional<Beneficiario> beneficiario;

    public BeneficiarioData(Optional<Beneficiario> beneficiario) {
        this.beneficiario = beneficiario;
    }

    public Beneficiario getBeneficiario() {
        return beneficiario.get();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>();
    }

    @Override
    public String getPassword() {
        return beneficiario.orElse(new Beneficiario()).getSenha();
    }

    @Override
    public String getUsername() {
        return beneficiario.orElse(new Beneficiario()).getEmail();
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

    public String getId() {
        return beneficiario.map(Beneficiario::getId).orElse(null);
    }
}

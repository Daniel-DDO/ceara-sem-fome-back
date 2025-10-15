package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.exception.CpfInvalidoException;
import com.ceara_sem_fome_back.exception.EmailJaCadastradoException;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        Optional<Administrador> administrador = administradorRepository.findByEmail(email);

        if (administrador.isPresent() && administrador.get().getSenha().equals(senha)) {
            return administrador.get();
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Administrador> administrador = administradorRepository.findByEmail(email);
        if (administrador.isEmpty()) {
            throw new UsernameNotFoundException("Usuário com email "+email+" não encontrado.");
        }
        return new AdministradorData(administrador);
    }

    public Administrador salvarAdm(Administrador administrador) {
        if (!verificarCpf(administrador.getCpf())) {
            throw new CpfInvalidoException(administrador.getCpf());
        }

        if (administradorRepository.findByEmail(administrador.getEmail()) != null) {
            throw new EmailJaCadastradoException(administrador.getEmail());
        }

        return administradorRepository.save(administrador);
    }

    public PaginacaoDTO<Administrador> listarTodos(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Administrador> pagina = administradorRepository.findAll(pageable);

        return new PaginacaoDTO<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getTotalPages(),
                pagina.getTotalElements(),
                pagina.getSize(),
                pagina.isLast()
        );
    }
}

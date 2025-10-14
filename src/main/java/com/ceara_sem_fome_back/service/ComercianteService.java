package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
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
public class ComercianteService implements UserDetailsService {

    @Autowired
    private ComercianteRepository comercianteRepository;

    public Comerciante logarComerciante(String email, String senha) {
        Optional<Comerciante> comerciante = comercianteRepository.findByEmail(email);

        if (comerciante.isPresent() && comerciante.get().getSenha().equals(senha)) {
            return comerciante.get();
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Comerciante> comerciante = comercianteRepository.findByEmail(email);
        if (comerciante.isEmpty()) {
            throw new UsernameNotFoundException("Usuário com email "+email+" não encontrado.");
        }
        return new ComercianteData(comerciante);
    }

    public Comerciante salvarComerciante(Comerciante comerciante) {

        if (comercianteRepository.existsById(comerciante.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        if (comercianteRepository.findByEmail(comerciante.getEmail()) != null) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        return comercianteRepository.save(comerciante);
    }

    public Page<Comerciante> listarTodos(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return comercianteRepository.findAll(pageable);
    }
}

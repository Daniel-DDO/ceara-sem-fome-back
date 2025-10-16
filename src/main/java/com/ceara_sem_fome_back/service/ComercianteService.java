package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.AdministradorRequest;
import com.ceara_sem_fome_back.dto.ComercianteRequest;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.exception.CpfJaCadastradoException;
import com.ceara_sem_fome_back.exception.EmailJaCadastradoException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ComercianteService implements UserDetailsService {

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CadastroService cadastroService;

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

    @Transactional
    public void iniciarCadastro(ComercianteRequest request) {
        checkIfUserExists(request.getCpf(), request.getEmail());
        cadastroService.criarTokenDeCadastroEVenviarEmailComerc(request);
    }

    private void checkIfUserExists(String cpf, String email) {
        if (comercianteRepository.findByEmail(email).isPresent()) {
            throw new EmailJaCadastradoException(email);
        }
        if (comercianteRepository.findByCpf(cpf).isPresent()) {
            throw new CpfJaCadastradoException(cpf);
        }
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
            throw new CpfJaCadastradoException(comerciante.getCpf());
        }

        if (comercianteRepository.findByEmail(comerciante.getEmail()) != null) {
            throw new EmailJaCadastradoException(comerciante.getEmail());
        }

        return comercianteRepository.save(comerciante);
    }

    public PaginacaoDTO<Comerciante> listarTodos(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comerciante> pagina = comercianteRepository.findAll(pageable);

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

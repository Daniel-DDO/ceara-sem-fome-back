package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
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
public class BeneficiarioService implements UserDetailsService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    public Beneficiario logarBeneficiario(String email, String senha) {
        Optional<Beneficiario> beneficiario = beneficiarioRepository.findByEmail(email);

        if (beneficiario.isPresent() && beneficiario.get().getSenha().equals(senha)) {
            return beneficiario.get();
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Beneficiario> beneficiario = beneficiarioRepository.findByEmail(email);
        if (beneficiario.isEmpty()) {
            throw new UsernameNotFoundException("Usuário com email "+email+" não encontrado.");
        }
        return new BeneficiarioData(beneficiario);
    }

    public Beneficiario salvarBeneficiario(Beneficiario beneficiario) {
        if (beneficiarioRepository.existsById(beneficiario.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        if (beneficiarioRepository.findByEmail(beneficiario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        return beneficiarioRepository.save(beneficiario);
    }

    //metodo pra listar em cada página
    public PaginacaoDTO<Beneficiario> listarTodos(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Beneficiario> pagina = beneficiarioRepository.findAll(pageable);

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

package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.EntregadorData;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.exception.CpfJaCadastradoException;
import com.ceara_sem_fome_back.exception.EmailJaCadastradoException;
import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.repository.EntregadorRepository;
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
public class EntregadorService implements UserDetailsService {

    @Autowired
    private EntregadorRepository entregadorRepository;

    public Entregador logarEntregador(String email, String senha) {
        Optional<Entregador> entregador = entregadorRepository.findByEmail(email);

        if (entregador.isPresent() && entregador.get().getSenha().equals(senha)) {
            return entregador.get();
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Entregador> entregador = entregadorRepository.findByEmail(email);
        if (entregador.isEmpty()) {
            throw new UsernameNotFoundException("Usuário com email "+email+" não encontrado.");
        }
        return new EntregadorData(entregador);
    }

    public Entregador salvarEntregador(Entregador entregador) {

        if (entregadorRepository.existsById(entregador.getCpf())) {
            throw new CpfJaCadastradoException(entregador.getCpf());
        }

        if (entregadorRepository.findByEmail(entregador.getEmail()) != null) {
            throw new EmailJaCadastradoException(entregador.getEmail());
        }
        return entregadorRepository.save(entregador);
    }

    public PaginacaoDTO<Entregador> listarTodos(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Entregador> pagina = entregadorRepository.findAll(pageable);

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

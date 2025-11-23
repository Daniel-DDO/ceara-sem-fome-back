package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.EntregadorData;
import com.ceara_sem_fome_back.dto.AlterarSenhaRequest;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.AlterarStatusRequest;
import com.ceara_sem_fome_back.dto.EntregadorRequest;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.exception.CpfInvalidoException;
import com.ceara_sem_fome_back.exception.CpfJaCadastradoException;
import com.ceara_sem_fome_back.exception.EmailJaCadastradoException;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.repository.EntregadorRepository;
import jakarta.persistence.EntityNotFoundException;
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
public class EntregadorService implements UserDetailsService {

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CadastroService cadastroService;

    @Autowired
    private NotificacaoService notificacaoService;

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

    @Transactional
    public void iniciarCadastro(EntregadorRequest request) {
        checkIfUserExists(request.getCpf(), request.getEmail());
        cadastroService.criarTokenDeCadastroEVenviarEmailEntreg(request);
    }

    private void checkIfUserExists(String cpf, String email) {
        if (entregadorRepository.findByEmail(email).isPresent()) {
            throw new EmailJaCadastradoException(email);
        }
        if (entregadorRepository.findByCpf(cpf).isPresent()) {
            throw new CpfJaCadastradoException(cpf);
        }
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
    public PaginacaoDTO<Entregador> listarComFiltro(
            String nomeFiltro,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Entregador> pagina;

        // Aplica o filtro se for válido
        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            pagina = entregadorRepository.findByNomeContainingIgnoreCase(nomeFiltro, pageable);
        } else {
            // Sem filtro, apenas paginação
            pagina = entregadorRepository.findAll(pageable);
        }

        return new PaginacaoDTO<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getTotalPages(),
                pagina.getTotalElements(),
                pagina.getSize(),
                pagina.isLast()
        );
    }

    public Entregador alterarStatusEntregador(AlterarStatusRequest request) {
        Entregador entregador = entregadorRepository.findById(request.getId())
                .orElseThrow(() -> new CpfInvalidoException("Comerciante não encontrado com o ID: " + request.getId()));

        entregador.setStatus(request.getNovoStatusPessoa());
        return entregadorRepository.save(entregador);
    }

    public Entregador filtrarPorCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new CpfInvalidoException(cpf);
        }

        return entregadorRepository.findByCpf(cpf)
                .orElseThrow(() -> new CpfInvalidoException(cpf));
    }

    @Autowired
    private RecaptchaService recaptchaService;

    @Transactional
    public void alterarSenha(String id, AlterarSenhaRequest request) {
        if (!recaptchaService.validarToken(request.getRecaptchaToken())) {
            throw new IllegalArgumentException("Erro no reCAPTCHA");
        }

        Entregador entregador = entregadorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entregador não encontrado"));

        if (!passwordEncoder.matches(request.getSenhaAtual(), entregador.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        entregador.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        entregadorRepository.save(entregador);
    }
}

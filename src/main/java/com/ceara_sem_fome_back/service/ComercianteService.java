package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.exception.*;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

import java.util.Objects;
import java.util.Optional;

@Service
public class ComercianteService implements UserDetailsService {

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private CompraService compraService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CadastroService cadastroService;

    @Autowired
    private ContaService contaService;

    @Autowired
    private NotificacaoService notificacaoService;

    public Comerciante logarComerciante(String email, String senha) {
        Optional<Comerciante> comerciante = comercianteRepository.findByEmail(email);

        if (comerciante.isPresent() && passwordEncoder.matches(senha, comerciante.get().getSenha())) {
            return comerciante.get();
        }

        throw new ContaNaoExisteException(email);
    }

    @Transactional
    public void iniciarCadastro(ComercianteRequest request) {
        cadastroService.validarCpfDisponivelEmTodosOsPerfis(request.getCpf());
        cadastroService.validarEmailDisponivelEmTodosOsPerfis(request.getEmail());
        cadastroService.criarTokenDeCadastroEVenviarEmailComerc(request);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Comerciante> comerciante = comercianteRepository.findByEmail(email);
        if (comerciante.isEmpty()) {
            throw new UsernameNotFoundException("Usuário com email " + email + " não encontrado.");
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

    public Comerciante alterarStatusComerciante(AlterarStatusRequest request) {
        Comerciante comerciante = comercianteRepository.findById(request.getId())
                .orElseThrow(() -> new CpfInvalidoException("Comerciante não encontrado com o ID: " + request.getId()));

        comerciante.setStatus(request.getNovoStatusPessoa());
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

    @Transactional
    public Comerciante atualizarComerciante(String userEmail, PessoaUpdateDto dto) {
        Comerciante comercianteExistente = comercianteRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comerciante não encontrado com o e-mail: " + userEmail));

        if (!Objects.equals(comercianteExistente.getEmail(), dto.getEmail())) {
            cadastroService.validarEmailDisponivelEmTodosOsPerfis(dto.getEmail());
            comercianteExistente.setEmail(dto.getEmail());
        }

        comercianteExistente.setNome(dto.getNome());
        comercianteExistente.setTelefone(dto.getTelefone());
        comercianteExistente.setDataNascimento(dto.getDataNascimento());
        comercianteExistente.setGenero(dto.getGenero());

        return comercianteRepository.save(comercianteExistente);
    }

    /*
    @Transactional(readOnly = true)
    public ContaDTO consultarExtrato(String comercianteId) {
        return compraService.calcularSaldoParaComerciante(comercianteId);
    }
     */

    public Comerciante buscarPorId(String id) {
        return comercianteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comerciante não encontrado com ID: " + id));
    }

    public Comerciante filtrarPorCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new CpfInvalidoException(cpf);
        }
        return buscarComerciantePorCpf(cpf);
    }

    public PaginacaoDTO<Comerciante> listarComFiltro(
            String nomeFiltro,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comerciante> pagina;

        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            pagina = comercianteRepository.findByNomeContainingIgnoreCase(nomeFiltro, pageable);
        } else {
            pagina = comercianteRepository.findAll(pageable);
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

    public Comerciante buscarComerciantePorCpf(String cpf) {
        return comercianteRepository.findByCpf(cpf)
                .orElseThrow(() -> new CpfInvalidoException(cpf));
    }

    public Comerciante buscarPorEmail(String email) {
        return comercianteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(email));
    }

    public void deletarPorId(String id) {
        Comerciante comerciante = buscarPorId(id);
        comercianteRepository.delete(comerciante);
    }

    public ComercianteRespostaDTO buscarPorIdDto(String comercianteId) {
        Comerciante comerciante = comercianteRepository.findById(comercianteId)
                .orElseThrow(() -> new RuntimeException("Comerciante não encontrado"));

        ComercianteRespostaDTO dto = new ComercianteRespostaDTO();

        dto.setId(comerciante.getId());
        dto.setNome(comerciante.getNome());
        dto.setCpf(comerciante.getCpf());
        dto.setEmail(comerciante.getEmail());
        dto.setDataNascimento(comerciante.getDataNascimento());
        dto.setTelefone(comerciante.getTelefone());
        dto.setGenero(comerciante.getGenero());
        dto.setLgpdAccepted(comerciante.getLgpdAccepted());
        dto.setStatus(comerciante.getStatus());
        dto.setConta(comerciante.getConta());

        return dto;
    }

}

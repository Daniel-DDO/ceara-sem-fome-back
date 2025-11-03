package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.*;
import org.springframework.context.annotation.Lazy;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.exception.*;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.exception.CpfInvalidoException;
import com.ceara_sem_fome_back.exception.CpfJaCadastradoException;
import com.ceara_sem_fome_back.exception.EmailJaCadastradoException;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ComercianteService implements UserDetailsService {

    @Autowired
    private ComercianteRepository comercianteRepository;

    // --- CORREÇÃO DA DEPENDÊNCIA CIRCULAR ---
    @Autowired
    @Lazy // Adiciona esta anotação
    private PasswordEncoder passwordEncoder;
    // --- FIM DA CORREÇÃO ---

    @Autowired
    private CadastroService cadastroService;

    public Comerciante logarComerciante(String email, String senha) {
        Optional<Comerciante> comerciante = comercianteRepository.findByEmail(email);

        //1. Usa passwordEncoder.matches() para comparar a senha criptografada
        if (comerciante.isPresent() && passwordEncoder.matches(senha, comerciante.get().getSenha())) {
            return comerciante.get();
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }

    @Transactional
    public void iniciarCadastro(ComercianteRequest request) {
        //2. Chama a validação CORRETA (cruzada)
        cadastroService.validarCpfDisponivelEmTodosOsPerfis(request.getCpf());
        cadastroService.validarEmailDisponivelEmTodosOsPerfis(request.getEmail());

        //3. Delega a criação do token
        cadastroService.criarTokenDeCadastroEVenviarEmailComerc(request);
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

    /**s
     * Atualiza os dados de um comerciante com base no seu e-mail (usuário)
     * pego da autenticação.
     *
     * @param userEmail E-mail do usuário autenticado (vem do token JWT).
     * @param dto Os novos dados para atualizar (PessoaUpdateDto).
     * @return O comerciante com os dados atualizados.
     */
    @Transactional
    public Comerciante atualizarComerciante(String userEmail, PessoaUpdateDto dto) {
        //1. Encontra o comerciante pelo e-mail do token
        Comerciante comercianteExistente = comercianteRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comerciante não encontrado com o e-mail: " + userEmail));

        //2. Verifica se o e-mail está sendo alterado
        if (!Objects.equals(comercianteExistente.getEmail(), dto.getEmail())) {
            //Se mudou, valida se o NOVO email já está em uso por QUALQUER pessoa
            cadastroService.validarEmailDisponivelEmTodosOsPerfis(dto.getEmail());
            comercianteExistente.setEmail(dto.getEmail());
        }

        //3. Atualiza os outros campos
        comercianteExistente.setNome(dto.getNome());
        comercianteExistente.setTelefone(dto.getTelefone());
        comercianteExistente.setDataNascimento(dto.getDataNascimento());
        comercianteExistente.setGenero(dto.getGenero());

        //4. Salva as alterações
        return comercianteRepository.save(comercianteExistente);
    }

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

        // Aplica o filtro se for válido
        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            pagina = comercianteRepository.findByNomeContainingIgnoreCase(nomeFiltro, pageable);
        } else {
            // Sem filtro, apenas paginação
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

}
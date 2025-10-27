package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.AdministradorRequest;
import com.ceara_sem_fome_back.dto.PessoaUpdateDto;
import com.ceara_sem_fome_back.exception.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
public class AdministradorService implements UserDetailsService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CadastroService cadastroService; // <-- Perfeito, já está aqui

    public Administrador logarAdm(String email, String senha) {
        Optional<Administrador> administrador = administradorRepository.findByEmail(email);

        //1. Usa passwordEncoder.matches() para comparar a senha criptografada
        if (administrador.isPresent() && passwordEncoder.matches(senha, administrador.get().getSenha())) {
            return administrador.get();
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }

    @Transactional
    public void iniciarCadastro(AdministradorRequest request) {
        //2. Chama a validação CORRETA (cruzada)
        cadastroService.validarCpfDisponivelEmTodosOsPerfis(request.getCpf());
        cadastroService.validarEmailDisponivelEmTodosOsPerfis(request.getEmail());

        //3. Delega a criação do token
        cadastroService.criarTokenDeCadastroEVenviarEmailAdm(request);
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

    /**
     * Atualiza os dados de um administrador com base no seu e-mail (usuário)
     * pego da autenticação.
     *
     * @param userEmail E-mail do usuário autenticado (vem do token JWT).
     * @param dto Os novos dados para atualizar (PessoaUpdateDto).
     * @return O administrador com os dados atualizados.
     */
    @Transactional
    public Administrador atualizarAdministrador(String userEmail, PessoaUpdateDto dto) {
        //1. Encontra o admin pelo e-mail do token
        Administrador adminExistente = administradorRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Administrador não encontrado com o e-mail: " + userEmail));

        //2. Verifica se o e-mail está sendo alterado
        if (!Objects.equals(adminExistente.getEmail(), dto.getEmail())) {
            //Se mudou, valida se o NOVO email já está em uso por QUALQUER pessoa
            cadastroService.validarEmailDisponivelEmTodosOsPerfis(dto.getEmail());
            adminExistente.setEmail(dto.getEmail());
        }

        //3. Atualiza os outros campos
        adminExistente.setNome(dto.getNome());
        adminExistente.setTelefone(dto.getTelefone());
        adminExistente.setDataNascimento(dto.getDataNascimento());
        adminExistente.setGenero(dto.getGenero());

        //4. Salva as alterações
        return administradorRepository.save(adminExistente);
    }

    /**
     * Chama o serviço de cadastro para reativar uma conta de qualquer tipo.
     * @param userId O ID do usuário a ser reativado.
     */
    @Transactional
    public void reativarConta(String userId) {
        // Delega a lógica de busca multi-repositório para o CadastroService
        cadastroService.reativarConta(userId);
    }
}
package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.AlterarStatusRequest;
import com.ceara_sem_fome_back.dto.BeneficiarioRequest;
import com.ceara_sem_fome_back.dto.PessoaUpdateDto;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.exception.CpfJaCadastradoException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Endereco;
import com.ceara_sem_fome_back.model.StatusPessoa;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import lombok.RequiredArgsConstructor;

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
@RequiredArgsConstructor
public class BeneficiarioService implements UserDetailsService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CadastroService cadastroService;

    public Beneficiario logarBeneficiario(String email, String senha) {
        Optional<Beneficiario> optionalBeneficiario = beneficiarioRepository.findByEmail(email);

        if (optionalBeneficiario.isPresent()) {
            Beneficiario beneficiario = optionalBeneficiario.get();
            if (passwordEncoder.matches(senha, beneficiario.getSenha())) {
                return beneficiario;
            }
        }
        throw new ContaNaoExisteException(email);
    }

    @Transactional
    public void iniciarCadastro(BeneficiarioRequest request) {
        //1. CHAMA A VALIDAÇÃO CORRETA (pública, que checa todos os perfis)
        cadastroService.validarCpfDisponivelEmTodosOsPerfis(request.getCpf());
        cadastroService.validarEmailDisponivelEmTodosOsPerfis(request.getEmail());

        //2. Delega a criação do token
        cadastroService.criarTokenDeCadastroEVenviarEmailBenef(request);
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
            throw new CpfJaCadastradoException(beneficiario.getCpf());
        }
        if (beneficiarioRepository.findByCpf(beneficiario.getCpf()).isPresent()) {
            throw new CpfJaCadastradoException(beneficiario.getCpf());
        }
        return beneficiario;
    }

    public Beneficiario alterarStatusBeneficiario(AlterarStatusRequest request) {
        Beneficiario beneficiario = beneficiarioRepository.findById(request.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado com o ID: " + request.getId()));

        beneficiario.setStatus(request.getNovoStatusPessoa());
        return beneficiarioRepository.save(beneficiario);
    }

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

    /**
     * Atualiza os dados de um beneficiário com base no seu e-mail (usuário)
     * pego da autenticação.
     *
     * @param userEmail E-mail do usuário autenticado (vem do token JWT).
     * @param dto Os novos dados para atualizar.
     * @return O beneficiário com os dados atualizados.
     */
    @Transactional
    public Beneficiario atualizarBeneficiario(String userEmail, PessoaUpdateDto dto) {
        //1. Encontra o beneficiário pelo e-mail do token
        Beneficiario beneficiarioExistente = beneficiarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado com o e-mail: " + userEmail));

        //2. Verifica se o e-mail está sendo alterado
        if (!Objects.equals(beneficiarioExistente.getEmail(), dto.getEmail())) {
            //Se o email mudou, validamos se o NOVO email já está em uso por QUALQUER pessoa
            cadastroService.validarEmailDisponivelEmTodosOsPerfis(dto.getEmail());
            
            //Se a validação passar, podemos setar o novo email
            beneficiarioExistente.setEmail(dto.getEmail());
        }

        //3. Atualiza os outros campos
        beneficiarioExistente.setNome(dto.getNome());
        beneficiarioExistente.setTelefone(dto.getTelefone());
        beneficiarioExistente.setDataNascimento(dto.getDataNascimento());
        beneficiarioExistente.setGenero(dto.getGenero()); //como String

        //4. Salva as alterações no banco
        return beneficiarioRepository.save(beneficiarioExistente);
    }

    //função parao beneficiário adicionar um endereço
//    @Transactional
//    public Beneficiario adicionarEndereco(String beneficiarioId, Endereco enderecoRequest) {
//        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
//                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado"));
//
//        entityManager.persist(enderecoRequest);
//        beneficiario.setEndereco(enderecoRequest);
//
//        return beneficiarioRepository.save(beneficiario);
//    }
}
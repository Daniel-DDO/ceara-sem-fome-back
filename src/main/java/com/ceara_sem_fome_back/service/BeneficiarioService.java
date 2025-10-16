package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.BeneficiarioRequest;
import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.exception.CpfJaCadastradoException;
import com.ceara_sem_fome_back.exception.EmailJaCadastradoException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BeneficiarioService implements UserDetailsService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // [MODIFICADO] Agora injeta o CadastroService para delegar a criação da conta
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

    // [MODIFICADO] A lógica complexa foi movida. Este método agora apenas valida e delega.
    @Transactional
    public void iniciarCadastro(BeneficiarioRequest request) {
        checkIfUserExists(request.getCpf(), request.getEmail());
        cadastroService.criarTokenDeCadastroEVenviarEmailBenef(request);
    }

    private void checkIfUserExists(String cpf, String email) {
        if (beneficiarioRepository.findByEmail(email).isPresent()) {
            throw new EmailJaCadastradoException(email);
        }
        if (beneficiarioRepository.findByCpf(cpf).isPresent()) {
            throw new CpfJaCadastradoException(cpf);
        }
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
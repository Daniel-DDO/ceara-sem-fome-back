package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.exception.*;
import com.ceara_sem_fome_back.mapper.BeneficiarioMapper;
import com.ceara_sem_fome_back.mapper.ComercianteMapper;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ceara_sem_fome_back.model.TipoPessoa.ADMINISTRADOR;

@Service
public class AdministradorService implements UserDetailsService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CadastroService cadastroService;

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

    public PaginacaoDTO<Administrador> listarComFiltro(
            String nomeFiltro,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Administrador> pagina;

        // 1. Aplica o filtro se for válido
        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            pagina = administradorRepository.findByNomeContainingIgnoreCase(nomeFiltro, pageable);
        } else {
            // Sem filtro, apenas paginação
            pagina = administradorRepository.findAll(pageable);
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

    public Pessoa alterarStatusAdministrador(AlterarStatusRequest request) {
        switch (request.getTipoPessoa()) {
            case ADMINISTRADOR -> {
                Administrador administrador = administradorRepository.findById(request.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Administrador não encontrado"));
                administrador.setStatus(request.getNovoStatusPessoa());
                return administradorRepository.save(administrador);
            }
            case BENEFICIARIO -> {
                Beneficiario beneficiario = beneficiarioRepository.findById(request.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado"));
                beneficiario.setStatus(request.getNovoStatusPessoa());
                return beneficiarioRepository.save(beneficiario);
            }
            case COMERCIANTE -> {
                Comerciante comerciante = comercianteRepository.findById(request.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Comerciante não encontrado"));
                comerciante.setStatus(request.getNovoStatusPessoa());
                return comercianteRepository.save(comerciante);
            }
            case ENTREGADOR -> {
                Entregador entregador = entregadorRepository.findById(request.getId())
                        .orElseThrow(() -> new EntityNotFoundException("Entregador não encontrado"));
                entregador.setStatus(request.getNovoStatusPessoa());
                return entregadorRepository.save(entregador);
            }
            default -> throw new IllegalArgumentException("Tipo de pessoa inválido.");
        }
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

    public Administrador filtrarPorCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new CpfInvalidoException(cpf);
        }
        return administradorRepository.findByCpf(cpf)
                .orElseThrow(() -> new CpfInvalidoException(cpf));
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

    public Produto aprovarProduto(String id, Administrador administradorLogado) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        switch (produto.getStatus()) {
            case AUTORIZADO:
                throw new RuntimeException("Este produto já está autorizado.");
            case RECUSADO:
                throw new RuntimeException("Este produto já foi recusado e não pode ser aprovado.");
            case PENDENTE:
                break;
            default:
                throw new RuntimeException("O produto está em um estado inválido para aprovação.");
        }

        produto.setStatus(StatusProduto.AUTORIZADO);
        produto.setAvaliadoPorId(administradorLogado);
        produto.setDataAvaliacao(LocalDateTime.now());

        return produtoRepository.save(produto);
    }

    public Produto recusarProduto(String id, Administrador administradorLogado) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        switch (produto.getStatus()) {
            case AUTORIZADO:
                throw new RuntimeException("Produto já está autorizado e não pode ser recusado.");
            case RECUSADO:
                throw new RuntimeException("Produto já foi recusado anteriormente.");
            case PENDENTE:
                break;
            default:
                throw new RuntimeException("O produto está em um estado inválido para recusa.");
        }

        produto.setStatus(StatusProduto.RECUSADO);
        produto.setAvaliadoPorId(administradorLogado);
        produto.setDataAvaliacao(LocalDateTime.now());

        return produtoRepository.save(produto);
    }

    // função para listar todos os estabelecimentos

    public List<Estabelecimento> listarTodosEstabelecimentos(Administrador adminLogado) {

        if (adminLogado == null) {
            throw new RuntimeException("Administrador não autenticado");
        }

        return estabelecimentoRepository.findAll();
    }

    public List<CompraRespostaDTO> verTodasAsCompras() {
        List<Compra> compras = compraRepository.findAll();
        List<CompraRespostaDTO> comprasDto = new ArrayList<>();

        for (Compra compra : compras) {

            CompraRespostaDTO dto = new CompraRespostaDTO();
            dto.setId(compra.getId());
            dto.setDataHora(compra.getDataHoraCompra());
            dto.setValorTotal(compra.getValorTotal());

            dto.setBeneficiarioId(compra.getBeneficiario().getId());
            dto.setBeneficiarioNome(compra.getBeneficiario().getNome());

            if (compra.getItens() != null && !compra.getItens().isEmpty()) {

                ProdutoCompra firstItem = compra.getItens().get(0);
                Estabelecimento estabelecimento = firstItem.getProdutoEstabelecimento().getEstabelecimento();
                Comerciante comerciante = estabelecimento.getComerciante();

                dto.setEstabelecimentoId(estabelecimento.getId());
                dto.setEstabelecimentoNome(estabelecimento.getNome());
                dto.setComercianteId(comerciante.getId());
                dto.setComercianteNome(comerciante.getNome());
            }

            List<CompraItemDTO> itensDto = new ArrayList<>();
            for (ProdutoCompra item : compra.getItens()) {

                CompraItemDTO itemDto = new CompraItemDTO(
                        item.getProdutoEstabelecimento().getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario()
                );
                itensDto.add(itemDto);
            }
            dto.setItens(itensDto);
            comprasDto.add(dto);
        }
        return comprasDto;
    }

    public List<Compra> verComprasPorBeneficiario(Beneficiario beneficiario) {
        return compraRepository.findByBeneficiario(beneficiario);
    }

    public List<Compra> verComprasPorBeneficiarioId(String beneficiarioId) {
        return compraRepository.findByBeneficiarioId(beneficiarioId);
    }

    public List<Compra> verComprasPorEstabelecimento(Estabelecimento estabelecimento) {
        return compraRepository.findDistinctByItensProdutoEstabelecimentoEstabelecimento(estabelecimento);
    }

    public List<Compra> verComprasPorEstabelecimentoId(String estabelecimentoId) {
        return compraRepository.findDistinctByItensProdutoEstabelecimentoEstabelecimentoId(estabelecimentoId);
    }

    public List<Compra> verComprasPorComerciante(Comerciante comerciante) {
        return compraRepository.findDistinctByItensProdutoEstabelecimentoEstabelecimentoComerciante(comerciante);
    }

    public List<Compra> verComprasPorComercianteId(String comercianteId) {
        return compraRepository.findDistinctByItensProdutoEstabelecimentoEstabelecimentoComercianteId(comercianteId);
    }

    // funções para o administrador conseguir informações de Beneficiário e do comerciante
    public List<ComercianteRespostaDTO> listarComerciantes() {
        return comercianteRepository.findAll()
                .stream()
                .map(ComercianteMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<BeneficiarioRespostaDTO> listarBeneficiarios() {
        return beneficiarioRepository.findAll()
                .stream()
                .map(BeneficiarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Se quiser um metodo único que retorne todos os dados juntos
    public DadosCompletosDTO obterDadosCompletos() {
        DadosCompletosDTO dto = new DadosCompletosDTO();
        dto.setComerciantes(listarComerciantes());
        dto.setBeneficiarios(listarBeneficiarios());
        // Adicione outros dados aqui
        return dto;
    }

    public Comerciante aprovarComerciante(String id) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comerciante não encontrado."));

        if (comerciante.getStatus().equals(StatusPessoa.ATIVO)) {
            throw new RuntimeException("Este comerciante já está ativo.");
        }

        comerciante.setStatus(StatusPessoa.ATIVO);
        return comercianteRepository.save(comerciante);
    }

    public Comerciante recusarComerciante(String id) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comerciante não encontrado."));

        if (comerciante.getStatus().equals(StatusPessoa.RECUSADO)) {
            throw new RuntimeException("Este comerciante já está recusado.");
        }

        comerciante.setStatus(StatusPessoa.RECUSADO);
        return comercianteRepository.save(comerciante);
    }

    public Comerciante bloquearComerciante(String id) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comerciante não encontrado."));

        if (comerciante.getStatus().equals(StatusPessoa.BLOQUEADO)) {
            throw new RuntimeException("Este comerciante já está bloqueado.");
        }

        comerciante.setStatus(StatusPessoa.BLOQUEADO);
        return comercianteRepository.save(comerciante);
    }

    public Comerciante inativarComerciante(String id) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comerciante não encontrado."));

        if (comerciante.getStatus().equals(StatusPessoa.INATIVO)) {
            throw new RuntimeException("Este comerciante já está inativo.");
        }

        comerciante.setStatus(StatusPessoa.INATIVO);
        return comercianteRepository.save(comerciante);
    }

    public Administrador buscarAdmPorId(String id) {
        return administradorRepository.findById(id).orElse(null);
    }

    public AdministradorRespostaDTO buscarPorIdDto(String administradorId) {
        Administrador admin = administradorRepository.findById(administradorId)
                .orElseThrow(() -> new RuntimeException("Administrador não encontrado"));

        AdministradorRespostaDTO dto = new AdministradorRespostaDTO();

        dto.setId(admin.getId());
        dto.setNome(admin.getNome());
        dto.setCpf(admin.getCpf());
        dto.setEmail(admin.getEmail());
        dto.setDataNascimento(admin.getDataNascimento());
        dto.setTelefone(admin.getTelefone());
        dto.setGenero(admin.getGenero());
        dto.setLgpdAccepted(admin.getLgpdAccepted());

        return dto;
    }

}
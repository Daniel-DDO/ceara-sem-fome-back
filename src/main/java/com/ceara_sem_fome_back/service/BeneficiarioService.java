package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.exception.*;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

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

    public PaginacaoDTO<Beneficiario> listarComFiltro(
            String nomeFiltro,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Beneficiario> pagina;

        // Aplica o filtro se for válido
        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            pagina = beneficiarioRepository.findByNomeContainingIgnoreCase(nomeFiltro, pageable);
        } else {
            // Sem filtro, apenas paginação
            pagina = beneficiarioRepository.findAll(pageable);
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

    public Beneficiario alterarStatusBeneficiario(AlterarStatusRequest request) {
        Beneficiario beneficiario = beneficiarioRepository.findById(request.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado com o ID: " + request.getId()));

        beneficiario.setStatus(request.getNovoStatusPessoa());
        return beneficiarioRepository.save(beneficiario);
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

    public List<Beneficiario> buscarPorBairro(String bairro) {
        return beneficiarioRepository.findByEnderecoBairro(bairro);
    }

    public List<Beneficiario> buscarPorMunicipio(String municipio) {
        return beneficiarioRepository.findByEnderecoMunicipio(municipio);
    }

    public Beneficiario filtrarPorCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new CpfInvalidoException(cpf);
        }

        return beneficiarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new CpfInvalidoException(cpf));

    }

    public BigDecimal verBalanco(String userEmail) {
        Beneficiario beneficiario = beneficiarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado."));

        Conta conta = beneficiario.getConta();
        if (conta == null) {
            throw new NegocioException("Conta não encontrada.", HttpStatus.NOT_FOUND);
        }
        return conta.getSaldo();
    }

    @Transactional(rollbackFor = {NegocioException.class, CarrinhoVazioException.class, SaldoInsuficienteException.class})
    public Compra realizarCompra(String userEmail) {
        Beneficiario beneficiario = beneficiarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado."));

        Carrinho carrinho = beneficiario.getCarrinho();
        List<ProdutoCarrinho> itensCarrinho = carrinho.getProdutos();

        Conta conta = beneficiario.getConta();
        if (conta == null) {
            throw new NegocioException("Conta bancária não encontrada. Não é possível realizar a compra.", HttpStatus.PRECONDITION_REQUIRED);
        }

        if (carrinho == null || itensCarrinho.isEmpty()) {
            throw new CarrinhoVazioException();
        }

        BigDecimal valorTotalBigDecimal = carrinho.getSubtotal();
        Double valorTotal = valorTotalBigDecimal.doubleValue();

        if (conta.getSaldo().compareTo(valorTotalBigDecimal) < 0) {
            throw new SaldoInsuficienteException(conta.getSaldo(), valorTotalBigDecimal);
        }


        Pageable firstOne = PageRequest.of(0, 1);
        List<Estabelecimento> estabelecimentos = estabelecimentoRepository.findAll(firstOne).getContent();

        Estabelecimento estabelecimento;
        if (estabelecimentos.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum estabelecimento disponível para compra.");
        } else {
            estabelecimento = estabelecimentos.get(0);
        }

        Endereco endereco = beneficiario.getEndereco();
        if (endereco == null) {
            throw new NegocioException("O beneficiário precisa ter um endereço cadastrado para realizar a compra.", HttpStatus.PRECONDITION_REQUIRED);
        }

        conta.setSaldo(conta.getSaldo().subtract(valorTotalBigDecimal));

        Compra novaCompra = new Compra();
        novaCompra.setBeneficiario(beneficiario);

        novaCompra.setDataHoraCompra(LocalDateTime.now());
        novaCompra.setValorTotal(valorTotal);
        novaCompra.setEstabelecimento(estabelecimento);
        novaCompra.setEndereco(endereco);
        novaCompra.setStatus(StatusCompra.FINALIZADA);

        List<ItemCompra> itensCompra = new ArrayList<>();

        for (ProdutoCarrinho pc : itensCarrinho) {
            ItemCompra ic = new ItemCompra();
            ic.setCompra(novaCompra);
            ic.setProduto(pc.getProduto());
            ic.setQuantidade(pc.getQuantidade());
            ic.setPrecoUnitario(pc.getProduto().getPreco());
            itensCompra.add(ic);
        }
        novaCompra.setItens(itensCompra);

        Compra compraSalva = compraRepository.save(novaCompra);

        carrinho.esvaziarCarrinho();
        carrinhoRepository.save(carrinho);

        beneficiarioRepository.save(beneficiario); // O Beneficiario salva a Conta em cascata

        return compraSalva;
    }

    public List<Compra> verHistoricoCompras(String userEmail) {
        Beneficiario beneficiario = beneficiarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado."));

        return beneficiario.getCompras();
    }

    public Carrinho verCarrinho(String userEmail) {
        Beneficiario beneficiario = beneficiarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado."));

        return beneficiario.getCarrinho();
    }

    @Transactional
    public Carrinho manipularCarrinho(String userEmail, String produtoId, int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior ou igual a zero.");
        }

        Beneficiario beneficiario = beneficiarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado."));

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null) {
            carrinho = new Carrinho();
            carrinho.setBeneficiario(beneficiario);
            carrinho = carrinhoRepository.save(carrinho);
            beneficiario.setCarrinho(carrinho);
        }

        Optional<ProdutoCarrinho> itemExistenteOpt = carrinho.getProdutos().stream()
                .filter(pc -> pc.getProduto().getId().equals(produtoId))
                .findFirst();

        if (quantidade == 0) {
            if (itemExistenteOpt.isPresent()) {
                carrinho.removerProduto(produto);
            }
        } else {
            if (itemExistenteOpt.isPresent()) {
                ProdutoCarrinho item = itemExistenteOpt.get();
                item.setQuantidade(quantidade);
            } else {
                carrinho.adicionarProduto(produto, quantidade);
            }
        }

        carrinho.atualizarSubtotal();

        return carrinhoRepository.save(carrinho);
    }

    public Beneficiario buscarPorId(String id) {
        return beneficiarioRepository.findById(id).orElse(null);
    }
}
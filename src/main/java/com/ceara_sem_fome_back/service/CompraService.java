package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ContaDTO;
import com.ceara_sem_fome_back.dto.HistoricoVendasDTO;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ReciboDTO;
import com.ceara_sem_fome_back.exception.EstoqueInsuficienteException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ceara_sem_fome_back.exception.SaldoInsuficienteException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompraService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ItemCompraRepository itemCompraRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoEstabelecimentoRepository produtoEstabelecimentoRepository;

    @Transactional
    public Compra finalizarCompra(String beneficiarioId, String estabelecimentoId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiario nao encontrado."));

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento nao encontrado."));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null) {
            throw new RecursoNaoEncontradoException("Carrinho nao encontrado para este beneficiario.");
        }

        List<ProdutoCarrinho> todosProdutosCarrinho = produtoCarrinhoRepository.findByCarrinho(carrinho);
        if (todosProdutosCarrinho.isEmpty()) {
            throw new RecursoNaoEncontradoException("O carrinho esta vazio.");
        }

        String comercianteId = estabelecimento.getComerciante().getId();
        List<ProdutoCarrinho> produtosParaComprar = todosProdutosCarrinho.stream()
                .filter(pc -> pc.getProdutoEstabelecimento().getProduto().getComerciante().getId().equals(comercianteId))
                .collect(Collectors.toList());

        if (produtosParaComprar.isEmpty()) {
            throw new RecursoNaoEncontradoException("Nenhum item no carrinho corresponde a este estabelecimento.");
        }

        BigDecimal valorTotal = BigDecimal.ZERO;
        List<ItemCompra> itensDaCompra = new ArrayList<>();

        for (ProdutoCarrinho pc : produtosParaComprar) {
            ProdutoEstabelecimento produtoEstabelecimento = produtoEstabelecimentoRepository.findById(pc.getProdutoEstabelecimento().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + pc.getProdutoEstabelecimento().getProduto().getNome() + " nao encontrado."));

            if (produtoEstabelecimento.getProduto().getQuantidadeEstoque() < pc.getQuantidade()) {
                throw new EstoqueInsuficienteException(
                        String.format("Estoque insuficiente para %s. Pedido: %d, Disponivel: %d",
                                produtoEstabelecimento.getProduto().getNome(), pc.getQuantidade(), produtoEstabelecimento.getProduto().getQuantidadeEstoque())
                );
            }

            BigDecimal subtotalItem = produtoEstabelecimento.getProduto().getPreco()
                    .multiply(BigDecimal.valueOf(pc.getQuantidade()));
            valorTotal = valorTotal.add(subtotalItem);

            ItemCompra item = new ItemCompra();
            item.setId(UUID.randomUUID().toString());
            item.setProdutoEstabelecimento(produtoEstabelecimento);
            item.setQuantidade(pc.getQuantidade());
            item.setPrecoUnitario(produtoEstabelecimento.getProduto().getPreco());
            itensDaCompra.add(item);
        }

        Conta contaBeneficiario = beneficiario.getConta();
        Conta contaComerciante = getConta(contaBeneficiario, valorTotal, estabelecimento);

        contaBeneficiario.setSaldo(contaBeneficiario.getSaldo().subtract(valorTotal));
        contaComerciante.setSaldo(contaComerciante.getSaldo().add(valorTotal));

        contaRepository.save(contaBeneficiario);
        contaRepository.save(contaComerciante);

        produtoService.decrementarEstoque(itensDaCompra);

        Compra compra = new Compra();
        compra.setId(UUID.randomUUID().toString());
        compra.setDataHoraCompra(LocalDateTime.now());
        compra.setStatus(StatusCompra.FINALIZADA);
        compra.setBeneficiario(beneficiario);
        compra.setEstabelecimento(estabelecimento);

        compra.setEndereco(estabelecimento.getEndereco()); // Salva o endereco DO ESTABELECIMENTO

        compra.setValorTotal(valorTotal.doubleValue());
        compraRepository.save(compra);

        for (ItemCompra item : itensDaCompra) {
            item.setCompra(compra);
            itemCompraRepository.save(item);
        }

        // Limpa APENAS os itens que foram comprados
        produtoCarrinhoRepository.deleteAll(produtosParaComprar);

        // Precisamos limpar a colecao na entidade Carrinho que esta na sessao
        // para que o proximo 'getProdutos()' nao retorne o cache antigo.
        carrinho.getProdutos().removeAll(produtosParaComprar);
        carrinhoRepository.save(carrinho); // Salva o carrinho com a colecao atualizada

        return compra;
    }

    private static Conta getConta(Conta contaBeneficiario, BigDecimal valorTotal, Estabelecimento estabelecimento) {
        if (contaBeneficiario == null) {
            throw new RecursoNaoEncontradoException("Conta do beneficiário não encontrada.");
        }

        if (contaBeneficiario.getSaldo().compareTo(valorTotal) < 0) {
            throw new SaldoInsuficienteException(
                    String.format("Saldo atual: R$ %.2f, Valor da compra: R$ %.2f",
                            contaBeneficiario.getSaldo(), valorTotal)
            );
        }

        Comerciante comerciante = estabelecimento.getComerciante();
        Conta contaComerciante = comerciante.getConta();
        if (contaComerciante == null) {
            throw new RecursoNaoEncontradoException("Conta do beneficiário não encontrada.");
        }
        return contaComerciante;
    }

    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    public List<Compra> listarPorBeneficiario(String beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado."));
        return compraRepository.findByBeneficiario(beneficiario);
    }

    @Transactional(readOnly = true)
    public List<HistoricoVendasDTO> getHistoricoVendasPorComerciante(String comercianteId) {
        List<Estabelecimento> estabelecimentos = estabelecimentoRepository.findByComercianteId(comercianteId);
        List<Compra> comprasDoComerciante = new ArrayList<>();
        for (Estabelecimento est : estabelecimentos) {
            comprasDoComerciante.addAll(compraRepository.findByEstabelecimentoId(est.getId()));
        }

        return comprasDoComerciante.stream().map(compra -> new HistoricoVendasDTO(
                compra.getId(),
                compra.getDataHoraCompra(),
                compra.getValorTotal(),
                compra.getBeneficiario().getNome(),
                compra.getEstabelecimento().getNome()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContaDTO calcularSaldoParaComerciante(String comercianteId) {
        List<Estabelecimento> estabelecimentos = estabelecimentoRepository.findByComercianteId(comercianteId);
        if (estabelecimentos.isEmpty()) {
            return new ContaDTO(BigDecimal.ZERO);
        }

        List<Compra> comprasDoComerciante = new ArrayList<>();
        for (Estabelecimento est : estabelecimentos) {
            comprasDoComerciante.addAll(compraRepository.findByEstabelecimentoId(est.getId()));
        }

        BigDecimal saldo = BigDecimal.ZERO;
        for (Compra compra : comprasDoComerciante) {
            saldo = saldo.add(BigDecimal.valueOf(compra.getValorTotal()));
        }

        return new ContaDTO(saldo);
    }

    public List<ItemCompra> listarItensDaCompra(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada."));
        return itemCompraRepository.findByCompra(compra);
    }

    public String gerarComprovante(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada."));

        List<ItemCompra> itens = itemCompraRepository.findByCompra(compra);

        StringBuilder sb = new StringBuilder();
        sb.append("=== COMPROVANTE DE COMPRA ===\n");
        sb.append("Beneficiário: ").append(compra.getBeneficiario().getNome()).append("\n");
        sb.append("Estabelecimento: ").append(compra.getEstabelecimento().getNome()).append("\n");
        sb.append("Data: ").append(compra.getDataHoraCompra()).append("\n\n");

        sb.append("Itens:\n");
        for (ItemCompra item : itens) {
            sb.append("- ").append(item.getProdutoEstabelecimento().getProduto().getNome())
                    .append(" | Qtd: ").append(item.getQuantidade())
                    .append(" | Preço: R$ ").append(item.getPrecoUnitario()).append("\n");
        }

        sb.append("\nTotal: R$ ").append(compra.getValorTotal()).append("\n");
        sb.append("===============================");

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public ReciboDTO obterReciboDTO(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada: " + compraId));

        List<ReciboDTO.ItemCompraDTO> itensDTO = compra.getItens().stream()
                .map(item -> new ReciboDTO.ItemCompraDTO(
                        item.getProdutoEstabelecimento().getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getValorTotalItem()
                )).collect(Collectors.toList());

        String enderecoCompleto = "Endereco nao cadastrado";
        Double lat = null;
        Double lon = null;

        if (compra.getEndereco() != null) {
            Endereco end = compra.getEndereco();
            enderecoCompleto = String.format("%s, %s - %s, %s",
                    end.getLogradouro(),
                    end.getNumero(),
                    end.getBairro(),
                    end.getMunicipio()
            );
            lat = end.getLatitude();
            lon = end.getLongitude();
        }

        return new ReciboDTO(
                compra.getId(),
                compra.getDataHoraCompra(),
                compra.getBeneficiario().getNome(),
                compra.getBeneficiario().getId(),
                compra.getEstabelecimento().getComerciante().getNome(),
                compra.getEstabelecimento().getNome(),
                enderecoCompleto,
                lat,
                lon,
                itensDTO,
                BigDecimal.valueOf(compra.getValorTotal())
        );
    }

    public List<Compra> listarPorEstabelecimentoEStatus(String estabelecimentoId, String status) {
        log.info("[SERVIÇO] Buscando compras para Estabelecimento ID: {} com Status: {}", estabelecimentoId, status);

        StatusCompra statusEnum = StatusCompra.valueOf(status.toUpperCase());

        return compraRepository.findByEstabelecimentoIdAndStatus(estabelecimentoId, statusEnum);
    }

    public PaginacaoDTO<Compra> listarComFiltro(String beneficiarioFiltro,
                                                int page,
                                                int size,
                                                String sortBy,
                                                String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Compra> pagina;

        if (beneficiarioFiltro == null || beneficiarioFiltro.isBlank()) {
            pagina = compraRepository.findAll(pageable);
        } else {
            pagina = compraRepository.findByBeneficiarioNomeContainingIgnoreCase(
                    beneficiarioFiltro, pageable
            );
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
}

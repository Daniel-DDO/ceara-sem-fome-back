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
import java.util.Map;
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
    private ProdutoCompraRepository produtoCompraRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoEstabelecimentoRepository produtoEstabelecimentoRepository;

    @Transactional
    public List<Compra> finalizarCompra(String beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado"));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null || carrinho.getProdutos().isEmpty()) {
            throw new RuntimeException("Carrinho vazio");
        }

        if (carrinho.getSubtotal().compareTo(beneficiario.getConta().getSaldo()) > 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        Map<String, List<ProdutoCarrinho>> produtosPorEstabelecimento = carrinho.getProdutos().stream()
                .collect(Collectors.groupingBy(pc -> pc.getProdutoEstabelecimento().getEstabelecimento().getId()));

        List<Compra> comprasCriadas = new ArrayList<>();

        for (List<ProdutoCarrinho> itensEstab : produtosPorEstabelecimento.values()) {
            BigDecimal valorTotalCompra = BigDecimal.ZERO;

            Compra compra = new Compra();
            compra.setId(UUID.randomUUID().toString());
            compra.setBeneficiario(beneficiario);
            compra.setDataHoraCompra(LocalDateTime.now());
            compra.setStatus(StatusCompra.ABERTA);

            List<ProdutoCompra> produtoCompras = new ArrayList<>();

            for (ProdutoCarrinho pcCarrinho : itensEstab) {
                ProdutoCompra pc = new ProdutoCompra();
                pc.setId(UUID.randomUUID().toString());
                pc.setCompra(compra);
                pc.setProdutoEstabelecimento(pcCarrinho.getProdutoEstabelecimento());
                pc.setQuantidade(pcCarrinho.getQuantidade());
                pc.setPrecoUnitario(pcCarrinho.getProdutoEstabelecimento().getProduto().getPreco());

                valorTotalCompra = valorTotalCompra.add(pc.getValorTotalItem());
                produtoCompras.add(pc);
            }

            compra.setItens(produtoCompras);
            compra.setValorTotal(valorTotalCompra.doubleValue());

            beneficiario.getConta().setSaldo(beneficiario.getConta().getSaldo().subtract(valorTotalCompra));

            compraRepository.save(compra);
            produtoCompraRepository.saveAll(produtoCompras);

            comprasCriadas.add(compra);
        }

        carrinho.esvaziarCarrinho();
        carrinhoRepository.save(carrinho);
        beneficiarioRepository.save(beneficiario);

        return comprasCriadas;
    }

    @Transactional
    public List<Compra> listarComprasBeneficiario(String beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado"));

        return compraRepository.findByBeneficiarioOrderByDataHoraCompraDesc(beneficiario);
    }

    @Transactional
    public Compra marcarComoRetirada(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

        if (compra.getStatus() != StatusCompra.ABERTA) {
            throw new RuntimeException("Só é possível marcar como retirada compras com status ABERTA");
        }

        compra.setStatus(StatusCompra.RETIRADA);
        return compraRepository.save(compra);
    }

    @Transactional
    public Compra marcarComoEntregue(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

        if (compra.getStatus() != StatusCompra.ABERTA) {
            throw new RuntimeException("Só é possível marcar como entregue compras com status ABERTA");
        }

        compra.setStatus(StatusCompra.ENTREGUE);
        return compraRepository.save(compra);
    }

    @Transactional
    public Compra reembolsarCompra(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

        if (compra.getStatus() != StatusCompra.ABERTA && compra.getStatus() != StatusCompra.RETIRADA) {
            throw new RuntimeException("Só é possível reembolsar compras com status ABERTA ou RETIRADA");
        }

        Beneficiario beneficiario = compra.getBeneficiario();
        BigDecimal valorTotal = BigDecimal.valueOf(compra.getValorTotal());
        beneficiario.getConta().setSaldo(beneficiario.getConta().getSaldo().add(valorTotal));

        compra.setStatus(StatusCompra.REEMBOLSADA);

        beneficiarioRepository.save(beneficiario);
        return compraRepository.save(compra);
    }

    @Transactional
    public List<HistoricoVendasDTO> listarVendasEstabelecimento(String estabelecimentoId) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        List<ProdutoCompra> vendas = produtoCompraRepository.findByProdutoEstabelecimento_Estabelecimento(estabelecimento);

        return vendas.stream().map(pc -> {
            HistoricoVendasDTO dto = new HistoricoVendasDTO();
            dto.setCompraId(pc.getCompra().getId());
            dto.setProdutoEstabelecimentoId(pc.getProdutoEstabelecimento().getId());
            dto.setQuantidade(pc.getQuantidade());
            dto.setPrecoUnitario(pc.getPrecoUnitario());
            dto.setDataCompra(pc.getCompra().getDataHoraCompra());
            dto.setBeneficiarioId(pc.getCompra().getBeneficiario().getId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Compra obterCompraPorId(String compraId) {
        return compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));
    }

    @Transactional
    public Compra atualizarStatusCompra(String compraId, StatusCompra novoStatus) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada"));

        compra.setStatus(novoStatus);
        return compraRepository.save(compra);
    }

    @Transactional
    public List<ReciboDTO> gerarRecibosPorComprao(String beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado"));

        List<Compra> compras = compraRepository.findByBeneficiarioOrderByDataHoraCompraDesc(beneficiario);

        List<ReciboDTO> recibos = new ArrayList<>();

        for (Compra compra : compras) {
            ReciboDTO recibo = new ReciboDTO();
            recibo.setCompraId(compra.getId());
            recibo.setDataCompra(compra.getDataHoraCompra());
            recibo.setNomeBeneficiario(compra.getBeneficiario().getNome());
            recibo.setBeneficiarioId(compra.getBeneficiario().getId());

            ProdutoEstabelecimento primeiroProduto = compra.getItens().get(0).getProdutoEstabelecimento();
            Estabelecimento estabelecimento = primeiroProduto.getEstabelecimento();

            recibo.setNomeComerciante(estabelecimento.getComerciante().getNome());
            recibo.setComercianteId(estabelecimento.getComerciante().getId());
            recibo.setNomeEstabelecimento(estabelecimento.getNome());
            recibo.setEnderecoEstabelecimentoCompleto(
                    estabelecimento.getEndereco().getLogradouro() + ", " +
                            estabelecimento.getEndereco().getNumero() + " - " +
                            estabelecimento.getEndereco().getBairro() + ", " +
                            estabelecimento.getEndereco().getMunicipio()
            );
            recibo.setLatitude(estabelecimento.getEndereco().getLatitude());
            recibo.setLongitude(estabelecimento.getEndereco().getLongitude());
            recibo.setValorTotal(BigDecimal.valueOf(compra.getValorTotal()));

            List<ReciboDTO.ItemCompraDTO> itensDTO = compra.getItens().stream().map(pc -> {
                ReciboDTO.ItemCompraDTO itemDTO = new ReciboDTO.ItemCompraDTO();
                itemDTO.setNomeProduto(pc.getProdutoEstabelecimento().getProduto().getNome());
                itemDTO.setProdutoEstabelecimentoId(pc.getProdutoEstabelecimento().getId());
                itemDTO.setProdutoId(pc.getProdutoEstabelecimento().getProduto().getId());
                itemDTO.setEstabelecimentoId(pc.getProdutoEstabelecimento().getEstabelecimento().getId());
                itemDTO.setQuantidade(pc.getQuantidade());
                itemDTO.setValorUnitario(pc.getPrecoUnitario());
                itemDTO.setSubtotal(pc.getValorTotalItem());
                return itemDTO;
            }).toList();

            recibo.setItens(itensDTO);
            recibos.add(recibo);
        }

        return recibos;
    }

    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    public List<Compra> listarPorBeneficiario(String beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado."));
        return compraRepository.findByBeneficiario(beneficiario);
    }

    public List<ProdutoCompra> listarItensDaCompra(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada."));
        return produtoCompraRepository.findByCompra(compra);
    }

}

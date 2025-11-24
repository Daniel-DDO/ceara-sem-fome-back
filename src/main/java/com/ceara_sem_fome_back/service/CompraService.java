package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.config.NotificacaoEvent;
import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.exception.EstoqueInsuficienteException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.exception.SaldoInsuficienteException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ProdutoEstabelecimentoRepository produtoEstabelecimentoRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public List<Compra> finalizarCompra(String beneficiarioId, List<String> idsSelecionados) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado"));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null || carrinho.getProdutos().isEmpty()) {
            throw new RuntimeException("Carrinho vazio");
        }

        List<ProdutoCarrinho> produtosSelecionados = carrinho.getProdutos().stream()
                .filter(pc -> idsSelecionados.contains(pc.getId()))
                .collect(Collectors.toList());

        if (produtosSelecionados.isEmpty()) {
            throw new RuntimeException("Nenhum item selecionado para compra");
        }

        BigDecimal totalSelecionado = produtosSelecionados.stream()
                .map(pc -> pc.getProdutoEstabelecimento().getProduto().getPreco().multiply(BigDecimal.valueOf(pc.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSelecionado.compareTo(beneficiario.getConta().getSaldo()) > 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        Map<String, List<ProdutoCarrinho>> produtosPorEstabelecimento = produtosSelecionados.stream()
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
            Estabelecimento estabelecimento = itensEstab.get(0).getProdutoEstabelecimento().getEstabelecimento();

            for (ProdutoCarrinho pcCarrinho : itensEstab) {
                ProdutoEstabelecimento pe = pcCarrinho.getProdutoEstabelecimento();

                if (pe.getProduto().getQuantidadeEstoque() < pcCarrinho.getQuantidade()) {
                    throw new RuntimeException("Estoque insuficiente para o produto: " + pe.getProduto().getNome());
                }

                ProdutoCompra pc = new ProdutoCompra();
                pc.setId(UUID.randomUUID().toString());
                pc.setCompra(compra);
                pc.setProdutoEstabelecimento(pe);
                pc.setQuantidade(pcCarrinho.getQuantidade());
                pc.setPrecoUnitario(pe.getProduto().getPreco());

                pe.getProduto().setQuantidadeEstoque(pe.getProduto().getQuantidadeEstoque() - pcCarrinho.getQuantidade());
                produtoEstabelecimentoRepository.save(pe);

                valorTotalCompra = valorTotalCompra.add(pc.getValorTotalItem());
                produtoCompras.add(pc);
            }

            beneficiario.getConta().setSaldo(beneficiario.getConta().getSaldo().subtract(valorTotalCompra));

            Conta contaComerciante = estabelecimento.getComerciante().getConta();
            contaComerciante.setSaldo(contaComerciante.getSaldo().add(valorTotalCompra));
            contaRepository.save(contaComerciante);

            compra.setItens(produtoCompras);
            compra.setValorTotal(valorTotalCompra.doubleValue());

            compraRepository.save(compra);
            produtoCompraRepository.saveAll(produtoCompras);

            comprasCriadas.add(compra);
        }

        try {
            eventPublisher.publishEvent(new NotificacaoEvent(this, beneficiarioId, "Compra realizada com sucesso."));
        } catch (Exception e) {
            log.error("Erro ao publicar evento de notificação", e);
        }

        produtoCarrinhoRepository.deleteAll(produtosSelecionados);
        carrinho.getProdutos().removeAll(produtosSelecionados);
        carrinhoRepository.save(carrinho);
        beneficiarioRepository.save(beneficiario);

        return comprasCriadas;
    }

    @Transactional
    public List<CompraDTO> listarComprasBeneficiario(String beneficiarioId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado"));

        List<Compra> compras = compraRepository.findByBeneficiarioOrderByDataHoraCompraDesc(beneficiario);

        return compras.stream().map(this::converterParaDTO).toList();
    }

    @Transactional
    public List<CompraDTO> listarComprasDoComerciante(String comercianteId) {
        List<Estabelecimento> estabelecimentos = estabelecimentoRepository.findByComercianteId(comercianteId);
        List<CompraDTO> resultado = new ArrayList<>();

        for (Estabelecimento est : estabelecimentos) {
            List<ProdutoCompra> vendas = produtoCompraRepository.findByProdutoEstabelecimento_Estabelecimento(est);
            Map<String, List<ProdutoCompra>> comprasAgrupadas = vendas.stream()
                    .collect(Collectors.groupingBy(pc -> pc.getCompra().getId()));

            for (List<ProdutoCompra> itensCompra : comprasAgrupadas.values()) {
                Compra compra = itensCompra.get(0).getCompra();
                CompraDTO dto = converterParaDTO(compra, est.getId());
                resultado.add(dto);
            }
        }

        resultado.sort((c1, c2) -> c2.getDataCompra().compareTo(c1.getDataCompra()));

        return resultado;
    }

    @Transactional
    public List<CompraDTO> listarTodas() {
        List<Compra> compras = compraRepository.findAll();

        return compras.stream()
                .map(this::converterParaDTO)
                .toList();
    }

    public CompraDTO converterParaDTO(Compra compra) {
        String estabelecimentoId = compra.getItens().get(0).getProdutoEstabelecimento().getEstabelecimento().getId();
        return converterParaDTO(compra, estabelecimentoId);
    }

    private CompraDTO converterParaDTO(Compra compra, String estabelecimentoId) {
        CompraDTO dto = new CompraDTO();
        dto.setCompraId(compra.getId());
        dto.setDataCompra(compra.getDataHoraCompra());
        dto.setBeneficiarioId(compra.getBeneficiario().getId());
        dto.setBeneficiarioNome(compra.getBeneficiario().getNome());
        dto.setEstabelecimentoId(estabelecimentoId);
        dto.setStatus(compra.getStatus());
        dto.setAvaliada(compra.isAvaliada());
        dto.setNomeEstabelecimento(
                compra.getItens().stream()
                        .filter(pc -> pc.getProdutoEstabelecimento().getEstabelecimento().getId().equals(estabelecimentoId))
                        .findFirst()
                        .map(pc -> pc.getProdutoEstabelecimento().getEstabelecimento().getNome())
                        .orElse("")
        );

        List<CompraDTO.ProdutoCompraDTO> itensDTO = compra.getItens().stream()
                .filter(pc -> pc.getProdutoEstabelecimento().getEstabelecimento().getId().equals(estabelecimentoId))
                .map(pc -> new CompraDTO.ProdutoCompraDTO(
                        pc.getProdutoEstabelecimento().getProduto().getId(),
                        pc.getProdutoEstabelecimento().getId(),
                        pc.getProdutoEstabelecimento().getProduto().getNome(),
                        pc.getQuantidade(),
                        pc.getPrecoUnitario(),
                        pc.getValorTotalItem()
                ))
                .toList();

        dto.setItens(itensDTO);
        return dto;
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

    public List<ProdutoCompra> listarItensDaCompra(String compraId) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Compra não encontrada."));
        return produtoCompraRepository.findByCompra(compra);
    }
}
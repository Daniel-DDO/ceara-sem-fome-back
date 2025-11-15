package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ContaDTO;
import com.ceara_sem_fome_back.dto.HistoricoVendasDTO;
import com.ceara_sem_fome_back.dto.ReciboDTO;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Compra finalizarCompra(String beneficiarioId, String estabelecimentoId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado."));

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado."));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null) {
            throw new RuntimeException("Carrinho não encontrado para este beneficiário.");
        }

        List<ProdutoCarrinho> produtosCarrinho = produtoCarrinhoRepository.findByCarrinho(carrinho);
        if (produtosCarrinho.isEmpty()) {
            throw new RuntimeException("O carrinho está vazio. Não é possível finalizar a compra.");
        }

        BigDecimal valorTotal = BigDecimal.ZERO;
        for (ProdutoCarrinho pc : produtosCarrinho) {
            valorTotal = valorTotal.add(
                    pc.getProduto().getPreco()
                            .multiply(BigDecimal.valueOf(pc.getQuantidade()))
            );
        }

        Conta contaBeneficiario = contaRepository.findByBeneficiario(beneficiario)
                .orElseThrow(() -> new RuntimeException("Conta do beneficiário não encontrada."));

        if (contaBeneficiario.getSaldo().compareTo(valorTotal) < 0) {
            throw new RuntimeException("Saldo insuficiente para realizar a compra.");
        }

        Comerciante comerciante = estabelecimento.getComerciante();
        Conta contaComerciante = contaRepository.findByComerciante(comerciante)
                .orElseThrow(() -> new RuntimeException("Conta do comerciante não encontrada."));

        contaBeneficiario.setSaldo(contaBeneficiario.getSaldo().subtract(valorTotal));
        contaComerciante.setSaldo(contaComerciante.getSaldo().add(valorTotal));

        contaRepository.save(contaBeneficiario);
        contaRepository.save(contaComerciante);

        Compra compra = new Compra();
        compra.setId(UUID.randomUUID().toString());
        compra.setDataHoraCompra(LocalDateTime.now());
        compra.setStatus(StatusCompra.FINALIZADA);
        compra.setBeneficiario(beneficiario);
        compra.setEstabelecimento(estabelecimento);
        compra.setEndereco(beneficiario.getEndereco());
        compra.setValorTotal(valorTotal.doubleValue());
        compraRepository.save(compra);

        for (ProdutoCarrinho pc : produtosCarrinho) {
            ItemCompra item = new ItemCompra();
            item.setId(UUID.randomUUID().toString());
            item.setCompra(compra);
            item.setProduto(pc.getProduto());
            item.setQuantidade(pc.getQuantidade());
            item.setPrecoUnitario(pc.getProduto().getPreco());
            itemCompraRepository.save(item);
        }

        produtoCarrinhoRepository.deleteAll(produtosCarrinho);

        return compra;
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
            sb.append("- ").append(item.getProduto().getNome())
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
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getValorTotalItem()
                )).collect(Collectors.toList());

        return new ReciboDTO(
                compra.getId(),
                compra.getDataHoraCompra(),
                compra.getBeneficiario().getNome(),
                compra.getBeneficiario().getId(),
                compra.getEstabelecimento().getComerciante().getNome(),
                compra.getEstabelecimento().getNome(),
                itensDTO,
                BigDecimal.valueOf(compra.getValorTotal())
        );
    }

    public List<Compra> listarPorEstabelecimentoEStatus(String estabelecimentoId, String status) {
        log.info("[SERVIÇO] Buscando compras para Estabelecimento ID: {} com Status: {}", estabelecimentoId, status);

        StatusCompra statusEnum = StatusCompra.valueOf(status.toUpperCase());

        return compraRepository.findByEstabelecimentoIdAndStatus(estabelecimentoId, statusEnum);
    }
}

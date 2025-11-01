package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompraService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ItemCompraRepository itemCompraRepository;

    public Compra finalizarCompra(String beneficiarioId, String estabelecimentoId) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
            .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado."));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null) {
            throw new RuntimeException("Carrinho não encontrado para este beneficiário.");
        }

        // Cria a compra
        Compra compra = new Compra();
        compra.setId(UUID.randomUUID().toString());
        compra.setDataHoraCompra(LocalDateTime.now());
        compra.setStatus("CONCLUIDA");
        compra.setBeneficiario(beneficiario);
        compra.setEstabelecimento(new Estabelecimento(estabelecimentoId)); // apenas id
        compra.setEndereco(beneficiario.getEndereco());

        BigDecimal valorTotal = BigDecimal.ZERO;

        List<ProdutoCarrinho> produtosCarrinho = produtoCarrinhoRepository.findByCarrinho(carrinho);
        for (ProdutoCarrinho pc : produtosCarrinho) {
            ItemCompra item = new ItemCompra();
            item.setId(UUID.randomUUID().toString());
            item.setCompra(compra);
            item.setProduto(pc.getProduto());
            item.setQuantidade(pc.getQuantidade());
            item.setPrecoUnitario(pc.getProduto().getPreco());
            itemCompraRepository.save(item);

            valorTotal = valorTotal.add(
                pc.getProduto().getPreco().multiply(BigDecimal.valueOf(pc.getQuantidade()))
            );
        }

        compra.setValorTotal(valorTotal);
        compraRepository.save(compra);

        // Limpa o carrinho após a compra
        produtoCarrinhoRepository.deleteAll(produtosCarrinho);

        return compra;
    }
}

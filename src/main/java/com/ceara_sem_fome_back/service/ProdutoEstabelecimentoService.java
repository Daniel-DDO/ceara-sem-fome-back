package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.model.Produto;
import com.ceara_sem_fome_back.model.ProdutoEstabelecimento;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoEstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProdutoEstabelecimentoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private ProdutoEstabelecimentoRepository produtoEstabelecimentoRepository;

    public void adicionarProdutoEmEstabelecimento(String produtoId, String estabelecimentoId) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        if (produtoEstabelecimentoRepository.existsByProdutoAndEstabelecimento(produto, estabelecimento)) {
            throw new RuntimeException("Este produto já está vinculado a este estabelecimento.");
        }

        ProdutoEstabelecimento produtoEstabelecimento = new ProdutoEstabelecimento();
        produtoEstabelecimento.setProduto(produto);
        produtoEstabelecimento.setEstabelecimento(estabelecimento);

        produtoEstabelecimentoRepository.save(produtoEstabelecimento);
    }

    public void removerProdutoDeEstabelecimento(String produtoId, String estabelecimentoId) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        ProdutoEstabelecimento pe = produtoEstabelecimentoRepository
                .findByProdutoAndEstabelecimento(produto, estabelecimento)
                .orElseThrow(() -> new RuntimeException("Este vínculo não existe."));

        produtoEstabelecimentoRepository.delete(pe);
    }

    public void atualizarEstoque(String produtoId, String estabelecimentoId, int novaQuantidade) {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        ProdutoEstabelecimento pe = produtoEstabelecimentoRepository
                .findByProdutoAndEstabelecimento(produto, estabelecimento)
                .orElseThrow(() -> new RuntimeException("O produto não está vinculado a este estabelecimento."));

        if (novaQuantidade > produto.getQuantidadeEstoque()) {
            throw new RuntimeException("A quantidade informada excede o estoque disponível do produto.");
        }

        produtoEstabelecimentoRepository.save(pe);
    }
}
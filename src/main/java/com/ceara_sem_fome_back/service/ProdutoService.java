package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ProdutoCadastroRequest;
import com.ceara_sem_fome_back.exception.AcessoNaoAutorizadoException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoEstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import com.ceara_sem_fome_back.model.Produto;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private ProdutoEstabelecimentoRepository produtoEstabelecimentoRepository;

    @Autowired
    private ComercianteService comercianteService;

    public ProdutoEstabelecimento cadastrarProduto(ProdutoCadastroRequest request, Comerciante comerciante) {

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(request.getEstabelecimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(request.getEstabelecimentoId()));

        if (!estabelecimento.getComerciante().getCpf().equals(comerciante.getCpf())) {
            throw new AcessoNaoAutorizadoException(comerciante.getCpf());
        }

        Produto novoProduto = new Produto();
        novoProduto.setId(UUID.randomUUID().toString());
        novoProduto.setNome(request.getNome());
        novoProduto.setPreco(request.getPrecoVenda().doubleValue());
        novoProduto.setQuantidadeEstoque(request.getEstoque());
        novoProduto.setComerciante(comerciante);
        novoProduto.setStatus(StatusProduto.PENDENTE);

        Produto produtoSalvo = produtoRepository.save(novoProduto);

        ProdutoEstabelecimento associacao = new ProdutoEstabelecimento();
        associacao.setProduto(produtoSalvo);
        associacao.setEstabelecimento(estabelecimento);

        return produtoEstabelecimentoRepository.save(associacao);
    }

    public List<ProdutoEstabelecimento> listarProdutosPorEstabelecimento(String estabelecimentoId) {

        List<ProdutoEstabelecimento> produtos = produtoEstabelecimentoRepository.findByEstabelecimento_Id(estabelecimentoId);
        return produtos;
    }


    public Produto aprovarProduto(String id) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));

        if (!produto.getStatus().equals(StatusProduto.PENDENTE)) {
            throw new IllegalStateException("Produto já foi autorizado, recusado ou desativado.");
        }

        produto.setStatus(StatusProduto.AUTORIZADO);
        produtoRepository.save(produto);

        return produto;
    }

    public Produto recusarProduto(String id) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));

        if (!produto.getStatus().equals(StatusProduto.PENDENTE)) {
            throw new IllegalStateException("Produto já foi autorizado, recusado ou desativado.");
        }

        produto.setStatus(StatusProduto.RECUSADO);
        produtoRepository.save(produto);

        return produto;
    }

    public Produto editarProduto(String id, String nome, String lote, String descricao, double preco, int quantidadeEstoque) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));

        if (!produto.getStatus().equals(StatusProduto.AUTORIZADO)) {
            throw new IllegalStateException("Produto não autorizado.");
        }

        produto.setNome(nome); produto.setLote(lote); produto.setDescricao(descricao);
        produto.setPreco(preco); produto.setQuantidadeEstoque(quantidadeEstoque);

        //só uma observação: acho que isso daqui deve mudar, pq dependendo do que o comerciante mudar,
        //o produto deveria voltar para o status de pendente, necessitando de uma reavaliação.

        produtoRepository.save(produto);

        return produto;
    }

    public Produto removerProduto(String id) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));

        if (produto.getStatus().equals(StatusProduto.RECUSADO)) {
            throw new IllegalStateException("Produto já foi recusado.");
        }
        else if (produto.getStatus().equals(StatusProduto.PENDENTE)) {
            throw new IllegalStateException("Produto pendente não pode ser removido.");
        }

        produto.setStatus(StatusProduto.DESATIVADO);
        produtoRepository.save(produto);

        return produto;
    }

    //imagem:

    @Transactional
    public Produto salvarImagem(String id, MultipartFile file) throws IOException {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem inválido ou ausente.");
        }

        produto.setImagem(file.getBytes());
        produto.setTipoImagem(file.getContentType()); //pode ser null dependendo do client
        return produtoRepository.save(produto);
    }

    //retorna bytes da imagem (ou null se não há imagem)
    @Transactional(readOnly = true)
    public byte[] buscarImagem(String id) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        return produto.getImagem();
    }

    @Transactional(readOnly = true)
    public String buscarTipoImagem(String id) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        return produto.getTipoImagem();
    }

    //remove apenas a imagem (mantém produto)
    @Transactional
    public void removerImagem(String id) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        produto.setImagem(null);
        produto.setTipoImagem(null);
        produtoRepository.save(produto);
    }

    //criar produto com imagem (criar produto + upload na requisição)
    @Transactional
    public Produto criarProdutoComImagem(Produto produto, MultipartFile file) throws IOException {
        if (produto == null || produto.getId() == null) {
            throw new IllegalArgumentException("Produto ou id ausente.");
        }
        if (file != null && !file.isEmpty()) {
            produto.setImagem(file.getBytes());
            produto.setTipoImagem(file.getContentType());
        }
        return produtoRepository.save(produto);
    }

    //verifica se tem imagem
    @Transactional(readOnly = true)
    public boolean possuiImagem(String id) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        return produto.getImagem() != null && produto.getImagem().length > 0;
    }

}
package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoCadastroRequest;
import com.ceara_sem_fome_back.dto.ProdutoDTO;
import com.ceara_sem_fome_back.exception.AcessoNaoAutorizadoException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoEstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException; // Import adicionado
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import com.ceara_sem_fome_back.model.Produto;
import org.springframework.web.multipart.MultipartFile; // Import adicionado

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

    @Transactional
    public Produto cadastrarProduto(ProdutoDTO produtoDTO, Comerciante comerciante, MultipartFile imagem) throws IOException {

        Produto novoProduto = new Produto();
        novoProduto.setId(UUID.randomUUID().toString());
        novoProduto.setNome(produtoDTO.getNome());
        novoProduto.setLote(produtoDTO.getLote());
        novoProduto.setDescricao(produtoDTO.getDescricao());
        novoProduto.setPreco(produtoDTO.getPreco());
        novoProduto.setQuantidadeEstoque(produtoDTO.getQuantidadeEstoque());
        novoProduto.setCategoriaProduto(produtoDTO.getCategoriaProduto());
        novoProduto.setComerciante(comerciante);
        novoProduto.setStatus(StatusProduto.PENDENTE);

        if (imagem != null && !imagem.isEmpty()) {
            String base64 = Base64.getEncoder().encodeToString(imagem.getBytes());
            novoProduto.setImagem(base64);
            novoProduto.setTipoImagem(imagem.getContentType());
        }

        return produtoRepository.save(novoProduto);
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

    public Produto editarProduto(String id, String nome, String lote, String descricao, BigDecimal preco, int quantidadeEstoque) {
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


    //retorna bytes da imagem (ou null se não há imagem)
    @Transactional(readOnly = true)
    public String buscarImagem(String id) {
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

    public Page<ProdutoEstabelecimento> listarProdutosComFiltroPorEstabelecimento(
            String estabelecimentoId,
            String nomeFiltro,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado."));

        // Verifica se o filtro de nome deve ser aplicado
        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            // Com filtro de nome
            return produtoEstabelecimentoRepository.findByEstabelecimento_IdAndProduto_NomeContainingIgnoreCase(
                    estabelecimentoId, nomeFiltro, pageable
            );
        } else {
            // Sem filtro de nome
            return produtoEstabelecimentoRepository.findByEstabelecimento_Id(estabelecimentoId, pageable);
        }
    }

    public PaginacaoDTO<Produto> listarComFiltro(
            String nomeFiltro,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Produto> pagina;

        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            pagina = produtoRepository.findByNomeContainingIgnoreCase(nomeFiltro, pageable);
        } else {
            pagina = produtoRepository.findAll(pageable);
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

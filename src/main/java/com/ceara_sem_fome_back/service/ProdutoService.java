package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoDTO;
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
        novoProduto.setCategoria(produtoDTO.getCategoria());
        novoProduto.setComerciante(comerciante);
        novoProduto.setStatus(StatusProduto.PENDENTE);

        if (imagem != null && !imagem.isEmpty()) {
            String base64 = Base64.getEncoder().encodeToString(imagem.getBytes());
            novoProduto.setImagem(base64);
            novoProduto.setTipoImagem(imagem.getContentType());
        }

        return produtoRepository.save(novoProduto);
    }

    private Produto alterarStatusProduto(String id, StatusProduto novoStatus) {
        Produto produto = produtoRepository.findByIdIgnoringStatus(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));

        if (!produto.getStatus().equals(StatusProduto.PENDENTE)) {
            throw new IllegalStateException("Produto já foi autorizado, recusado ou desativado.");
        }

        produto.setStatus(novoStatus);
        return produtoRepository.save(produto);
    }

    public Produto aprovarProduto(String id) {
        return alterarStatusProduto(id, StatusProduto.AUTORIZADO);
    }

    public Produto recusarProduto(String id) {
        return alterarStatusProduto(id, StatusProduto.RECUSADO);
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

        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            return produtoEstabelecimentoRepository.findByEstabelecimento_IdAndProduto_NomeContainingIgnoreCase(
                    estabelecimentoId, nomeFiltro, pageable
            );
        } else {
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

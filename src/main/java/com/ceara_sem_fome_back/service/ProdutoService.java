package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.AtualizarEstoqueDTO;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoDTO;
import com.ceara_sem_fome_back.exception.EstoqueInsuficienteException;
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

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private ProdutoEstabelecimentoRepository produtoEstabelecimentoRepository;

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
        novoProduto.setUnidade(produtoDTO.getUnidade());
        novoProduto.setComerciante(comerciante);
        novoProduto.setAvaliadoPorId(null);
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

    public Produto aprovarProduto(String id, Administrador administradorLogado) {
        return alterarStatusProduto(id, StatusProduto.AUTORIZADO);
    }

    public Produto recusarProduto(String id, Administrador administradorLogado) {
        return alterarStatusProduto(id, StatusProduto.RECUSADO);
    }

    @Transactional
    public Produto editarProdutoComImagem(ProdutoDTO produtoDTO, MultipartFile imagem) throws IOException {
        if (produtoDTO.getId() == null || produtoDTO.getId().isEmpty()) {
            throw new IllegalArgumentException("O ID do produto é obrigatório para a edição.");
        }

        Produto produtoExistente = produtoRepository.findByIdIgnoringStatus(produtoDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado para edição."));

        if (!produtoExistente.getStatus().equals(StatusProduto.AUTORIZADO)) {
            throw new IllegalStateException("Produto não autorizado.");
        }

        produtoExistente.setNome(produtoDTO.getNome());
        produtoExistente.setLote(produtoDTO.getLote());
        produtoExistente.setDescricao(produtoDTO.getDescricao());
        produtoExistente.setPreco(produtoDTO.getPreco());
        produtoExistente.setQuantidadeEstoque(produtoDTO.getQuantidadeEstoque());

        if (produtoDTO.getCategoria() != null) {
            produtoExistente.setCategoria(produtoDTO.getCategoria());
        }
        if (produtoDTO.getUnidade() != null) {
            produtoExistente.setUnidade(produtoDTO.getUnidade());
        }

        if (imagem != null && !imagem.isEmpty()) {
            String base64 = Base64.getEncoder().encodeToString(imagem.getBytes());
            produtoExistente.setImagem(base64);
            produtoExistente.setTipoImagem(imagem.getContentType());
        }

        produtoExistente.setStatus(StatusProduto.PENDENTE); //para ser avaliado novamente

        return produtoRepository.save(produtoExistente);
    }

    @Transactional
    public Produto atualizarEstoque(String produtoId, AtualizarEstoqueDTO dto, Comerciante comerciante) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o ID: " + produtoId));

        if (!Objects.equals(produto.getComerciante().getId(), comerciante.getId())) {
            throw new SecurityException("Este produto não pertence ao comerciante autenticado.");
        }

        if (produto.getStatus() != StatusProduto.AUTORIZADO) {
            throw new IllegalStateException("Apenas produtos autorizados podem ter seu estoque atualizado.");
        }

        if (dto.getNovaQuantidade() < 0) {
            throw new IllegalArgumentException("A quantidade em estoque não pode ser negativa.");
        }

        produto.setQuantidadeEstoque(dto.getNovaQuantidade());

        return produtoRepository.save(produto);
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

    public List<ProdutoDTO> listarPorComerciante(String comercianteId) {
        List<Produto> produtos = produtoRepository.findByComercianteId(comercianteId);

        return produtos.stream().map(
                p -> new ProdutoDTO(
                        p.getId(),
                        p.getNome(),
                        p.getLote(),
                        p.getDescricao(),
                        p.getPreco(),
                        p.getQuantidadeEstoque(),
                        p.getStatus(),
                        p.getCategoria(),
                        p.getUnidade(),
                        p.getImagem(),
                        p.getTipoImagem(),
                        p.getDataCadastro(),
                        p.getAvaliadoPorId(),
                        p.getDataAvaliacao(),
                        p.getComerciante().getId()
                )).collect(Collectors.toList());
    }

    public List<Produto> filtrarProdutosPorNome(String pesquisa) {
        return produtoRepository.buscaInteligente(pesquisa);
    }

    @Transactional
    public void decrementarEstoque(List<ItemCompra> itens) {
        for (ItemCompra item : itens) {
            Produto produto = produtoRepository.findById(item.getProduto().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + item.getProduto().getNome() + " nao encontrado durante a baixa de estoque."));

            int novoEstoque = produto.getQuantidadeEstoque() - item.getQuantidade();
            if (novoEstoque < 0) {
                //falha critica
                throw new EstoqueInsuficienteException("Falha critica de concorrencia no estoque do produto: " + produto.getNome());
            }

            produto.setQuantidadeEstoque(novoEstoque);
            produtoRepository.save(produto);
        }
    }
}
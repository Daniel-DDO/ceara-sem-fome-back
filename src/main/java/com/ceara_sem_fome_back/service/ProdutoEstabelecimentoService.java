package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoEstabDTO;
import com.ceara_sem_fome_back.model.Endereco;
import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.model.Produto;
import com.ceara_sem_fome_back.model.ProdutoEstabelecimento;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoEstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public PaginacaoDTO<ProdutoEstabDTO> listarComFiltro(
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
        Page<ProdutoEstabelecimento> pagina;

        if (nomeFiltro != null && !nomeFiltro.trim().isEmpty()) {
            pagina = produtoEstabelecimentoRepository
                    .findByProduto_NomeContainingIgnoreCase(nomeFiltro.trim(), pageable);
        } else {
            pagina = produtoEstabelecimentoRepository.findAll(pageable);
        }

        Page<ProdutoEstabDTO> paginaDTO = pagina.map(this::toDTO);

        return new PaginacaoDTO<>(
                paginaDTO.getContent(),
                paginaDTO.getNumber(),
                paginaDTO.getTotalPages(),
                paginaDTO.getTotalElements(),
                paginaDTO.getSize(),
                paginaDTO.isLast()
        );
    }

    public ProdutoEstabDTO toDTO(ProdutoEstabelecimento pe) {

        ProdutoEstabDTO dto = new ProdutoEstabDTO();

        Produto p = pe.getProduto();
        Estabelecimento e = pe.getEstabelecimento();

        dto.setId(pe.getId());
        dto.setNomeProduto(p.getNome());
        dto.setDescricao(p.getDescricao());
        dto.setNomeEstabelecimento(e.getNome());

        dto.setPreco(p.getPreco());
        dto.setQuantidadeEstoque(p.getQuantidadeEstoque());
        dto.setCategoria(p.getCategoria());
        dto.setUnidade(p.getUnidade());
        dto.setDataCadastro(p.getDataCadastro());

        dto.setImagem(p.getImagem());
        dto.setTipoImagem(p.getTipoImagem());

        dto.setEndereco(e.getEndereco());
        dto.setComercianteId(e.getComerciante().getId());

        return dto;
    }

    public ProdutoEstabelecimento buscarPorProdutoId(String produtoId) {
        return produtoEstabelecimentoRepository.findByProdutoId(produtoId).orElse(null);
    }

    public ProdutoEstabelecimento buscarPorId(String idProdEstab) {
        return produtoEstabelecimentoRepository.findById(idProdEstab).orElse(null);
    }

    public PaginacaoDTO<ProdutoEstabDTO> listarPorEstabelecimento(
            String estabelecimentoId,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProdutoEstabelecimento> pagina =
                produtoEstabelecimentoRepository.findByEstabelecimento_Id(estabelecimentoId, pageable);

        Page<ProdutoEstabDTO> paginaDTO = pagina.map(this::toDTO);

        return new PaginacaoDTO<>(
                paginaDTO.getContent(),
                paginaDTO.getNumber(),
                paginaDTO.getTotalPages(),
                paginaDTO.getTotalElements(),
                paginaDTO.getSize(),
                paginaDTO.isLast()
        );
    }

}
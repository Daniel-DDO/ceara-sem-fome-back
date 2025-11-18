package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.EstabelecimentoRespostaDTO;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoDTO;
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

        ProdutoDTO produtoDto = new ProdutoDTO();
        EstabelecimentoRespostaDTO estabelecimentoDto = new EstabelecimentoRespostaDTO();

        produtoDto.setId(p.getId());
        produtoDto.setNome(p.getNome());
        produtoDto.setLote(p.getLote());
        produtoDto.setDescricao(p.getDescricao());
        produtoDto.setPreco(p.getPreco());
        produtoDto.setQuantidadeEstoque(p.getQuantidadeEstoque());
        produtoDto.setStatus(p.getStatus());
        produtoDto.setCategoria(p.getCategoria());
        produtoDto.setUnidade(p.getUnidade());
        produtoDto.setImagem(p.getImagem());
        produtoDto.setTipoImagem(p.getTipoImagem());
        produtoDto.setDataCadastro(p.getDataCadastro());
        produtoDto.setAvaliadoPorId(p.getAvaliadoPorId());
        produtoDto.setDataAvaliacao(p.getDataAvaliacao());
        produtoDto.setComercianteId(p.getComerciante().getId());

        estabelecimentoDto.setId(e.getId());
        estabelecimentoDto.setNome(e.getNome());
        estabelecimentoDto.setCnpj(e.getCnpj());
        estabelecimentoDto.setTelefone(e.getTelefone());
        estabelecimentoDto.setImagem(e.getImagem());
        estabelecimentoDto.setTipoImagem(e.getTipoImagem());
        estabelecimentoDto.setEnderecoId(e.getEndereco().getId());
        estabelecimentoDto.setCep(e.getEndereco().getCep());
        estabelecimentoDto.setLogradouro(e.getEndereco().getLogradouro());
        estabelecimentoDto.setNumero(e.getEndereco().getNumero());
        estabelecimentoDto.setBairro(e.getEndereco().getBairro());
        estabelecimentoDto.setMunicipio(e.getEndereco().getMunicipio());

        dto.setId(pe.getId());
        dto.setProdutoDTO(produtoDto);
        dto.setEstabelecimentoRespostaDTO(estabelecimentoDto);

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

    public void deletarProdEstab(String idProdEstab) {
        produtoEstabelecimentoRepository.deletarProdEstab(idProdEstab);
    }
}
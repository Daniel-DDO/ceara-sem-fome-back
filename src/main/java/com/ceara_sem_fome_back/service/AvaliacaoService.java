package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.AvaliacaoRequestDTO;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.RespostaAvaliacaoRequestDTO;
import com.ceara_sem_fome_back.exception.NegocioException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvaliacaoService {

    private final CompraRepository compraRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final ComercianteRepository comercianteRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    @Transactional
    public void registrarAvaliacao(String compraId, String beneficiarioId, AvaliacaoRequestDTO dto) {

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Compra não encontrada com ID: " + compraId));

        if (!compra.getBeneficiario().getId().equals(beneficiarioId)) {
            throw new NegocioException("Acesso negado. Esta compra não pertence ao seu perfil.", HttpStatus.FORBIDDEN);
        }

        if (!compra.getStatus().equals(StatusCompra.FINALIZADA)) {
            String mensagem = String.format(
                    "A compra precisa estar no status '%s' para ser avaliada. Status atual: %s",
                    StatusCompra.FINALIZADA.name(),
                    compra.getStatus().name()
            );
            throw new NegocioException(mensagem, HttpStatus.BAD_REQUEST);
        }

        //if (compra.getAvaliacao() != null) {
        //    throw new NegocioException("Esta compra já foi avaliada.", HttpStatus.BAD_REQUEST);
        //}

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setCompra(compra);
        avaliacao.setEstrelas(dto.getEstrelas());
        avaliacao.setComentario(dto.getComentario());
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        Avaliacao avaliacaoSalva = avaliacaoRepository.save(avaliacao);

        //compra.setAvaliacao(avaliacaoSalva);
        compraRepository.save(compra);

        //atualizarMedias(compra, dto.getEstrelas());

        log.info("Avaliação registrada com sucesso | Compra ID: {} | Beneficiário ID: {} | Estrelas: {}",
                compraId, beneficiarioId, dto.getEstrelas());
    }

    @Transactional
    public void registrarRespostaComerciante(String comercianteId, RespostaAvaliacaoRequestDTO dto) {

        Avaliacao avaliacao = avaliacaoRepository.findById(dto.getAvaliacaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada com ID: " + dto.getAvaliacaoId()));

        Compra compra = avaliacao.getCompra();


        //if (!compra.getEstabelecimento().getComerciante().getId().equals(comercianteId)) {
        //    throw new NegocioException("Acesso negado. Este comerciante não pode responder a esta avaliação.", HttpStatus.FORBIDDEN);
        //}

        if (avaliacao.getRespostaComerciante() != null && !avaliacao.getRespostaComerciante().isBlank()) {
            throw new NegocioException("Esta avaliação já foi respondida.", HttpStatus.BAD_REQUEST);
        }

        avaliacao.setRespostaComerciante(dto.getResposta());
        avaliacao.setDataResposta(LocalDateTime.now());

        avaliacaoRepository.save(avaliacao);

        log.info("Resposta do comerciante registrada | Avaliação ID: {} | Comerciante ID: {}",
                dto.getAvaliacaoId(), comercianteId);
    }

    @Transactional(readOnly = true)
    public PaginacaoDTO<Avaliacao> listarAvaliacoes(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        String campoOrdenacao;
        if (sortBy.equalsIgnoreCase("recentes")) {
            campoOrdenacao = "dataAvaliacao";
            sortDirection = Sort.Direction.DESC;
        } else if (sortBy.equalsIgnoreCase("melhores")) {
            campoOrdenacao = "estrelas";
            sortDirection = Sort.Direction.DESC;
        } else {
            campoOrdenacao = "dataAvaliacao";
            sortDirection = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(sortDirection, campoOrdenacao);

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Avaliacao> pagina = avaliacaoRepository.findAll(pageable);

        return new PaginacaoDTO<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getTotalPages(),
                pagina.getTotalElements(),
                pagina.getSize(),
                pagina.isLast()
        );
    }

    /*
    private void atualizarMedias(Compra compra, Integer novaNota) {


        compra.getItens().forEach(item -> {
            Produto produto = item.getProdutoEstabelecimento().getProduto();

            Double novaMediaProduto = avaliacaoRepository.findAverageByProdutoEstabelecimentoId(produto.getId());

            produto.setMediaAvaliacoes(novaMediaProduto);
            produtoRepository.save(produto);
        });

        Estabelecimento estabelecimento = compra.getEstabelecimento();

        Double novaMediaEstabelecimento = avaliacaoRepository.findAverageByEstabelecimentoId(estabelecimento.getId());

        estabelecimento.setMediaAvaliacoes(novaMediaEstabelecimento);
        estabelecimentoRepository.save(estabelecimento);

        Comerciante comerciante = estabelecimento.getComerciante();

        Double novaMediaComerciante = avaliacaoRepository.findAverageByComercianteId(comerciante.getId());

        comerciante.setMediaAvaliacoes(novaMediaComerciante);
        comercianteRepository.save(comerciante);
    }

     */
}
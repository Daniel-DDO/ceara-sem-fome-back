package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.AvaliacaoRequestDTO;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.RespostaAvaliacaoRequestDTO;
import com.ceara_sem_fome_back.exception.NegocioException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@Slf4j
public class AvaliacaoService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Transactional
    public void registrarAvaliacao(String compraId, String beneficiarioId, AvaliacaoRequestDTO dto) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Compra não encontrada com ID: " + compraId));

        if (!compra.getBeneficiario().getId().equals(beneficiarioId)) {
            throw new NegocioException("Acesso negado. Esta compra não pertence ao seu perfil.", HttpStatus.FORBIDDEN);
        }

        Set<StatusCompra> statusPermitidos = Set.of(
                StatusCompra.RETIRADA,
                StatusCompra.ENTREGUE,
                StatusCompra.FINALIZADA
        );

        if (!statusPermitidos.contains(compra.getStatus())) {
            throw new NegocioException(
                    String.format("Status da compra inválido para avaliação: %s", compra.getStatus()),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (compra.isAvaliada()) {
            throw new NegocioException("Esta compra já foi avaliada.", HttpStatus.BAD_REQUEST);
        }

        if (dto.getEstrelas() < 1 || dto.getEstrelas() > 5) {
            throw new NegocioException("A nota deve ser entre 1 e 5 estrelas.", HttpStatus.BAD_REQUEST);
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setCompra(compra);
        avaliacao.setEstrelas(dto.getEstrelas());
        avaliacao.setComentario(dto.getComentario());
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        avaliacaoRepository.save(avaliacao);

        compra.setAvaliada(true);
        compraRepository.save(compra);

        atualizarMedias(compra);

        log.info("Avaliação registrada com sucesso | Compra ID: {} | Beneficiário ID: {}", compraId, beneficiarioId);
    }

    @Transactional
    public void registrarRespostaComerciante(String comercianteId, RespostaAvaliacaoRequestDTO dto) {
        Avaliacao avaliacao = avaliacaoRepository.findById(dto.getAvaliacaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada com ID: " + dto.getAvaliacaoId()));

        Compra compra = avaliacao.getCompra();

        if (compra.getItens().isEmpty()) {
            throw new NegocioException("Inconsistência de dados: Compra sem itens.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Estabelecimento estabelecimento = compra.getItens().get(0).getProdutoEstabelecimento().getEstabelecimento();
        Comerciante comerciante = estabelecimento.getComerciante();

        if (!comerciante.getId().equals(comercianteId)) {
            throw new NegocioException("Acesso negado. Este comerciante não pode responder a esta avaliação.", HttpStatus.FORBIDDEN);
        }

        if (avaliacao.getRespostaComerciante() != null && !avaliacao.getRespostaComerciante().isBlank()) {
            throw new NegocioException("Esta avaliação já foi respondida.", HttpStatus.BAD_REQUEST);
        }

        avaliacao.setRespostaComerciante(dto.getResposta());
        avaliacao.setDataResposta(LocalDateTime.now());

        avaliacaoRepository.save(avaliacao);

        log.info("Resposta registrada | Avaliação ID: {} | Comerciante ID: {}", dto.getAvaliacaoId(), comercianteId);
    }

    @Transactional(readOnly = true)
    public PaginacaoDTO<Avaliacao> listarAvaliacoes(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String campoOrdenacao = "dataAvaliacao";

        if ("melhores".equalsIgnoreCase(sortBy)) {
            campoOrdenacao = "estrelas";
            sortDirection = Sort.Direction.DESC;
        } else if ("piores".equalsIgnoreCase(sortBy)) {
            campoOrdenacao = "estrelas";
            sortDirection = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, campoOrdenacao));
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

    private void atualizarMedias(Compra compra) {
        if (compra.getItens() == null || compra.getItens().isEmpty()) {
            return;
        }

        compra.getItens().forEach(item -> {
            ProdutoEstabelecimento produtoEstabelecimento = item.getProdutoEstabelecimento();
            Produto produto = produtoEstabelecimento.getProduto();

            Double novaMediaProduto = avaliacaoRepository.findAverageByProdutoEstabelecimentoId(produtoEstabelecimento.getId());

            if (novaMediaProduto != null) {
                produto.setMediaAvaliacoes(novaMediaProduto);
                produtoRepository.save(produto);
            }
        });

        Estabelecimento estabelecimento = compra.getItens().get(0).getProdutoEstabelecimento().getEstabelecimento();
        Double novaMediaEstabelecimento = avaliacaoRepository.findAverageByEstabelecimentoId(estabelecimento.getId());

        if (novaMediaEstabelecimento != null) {
            estabelecimento.setMediaAvaliacoes(novaMediaEstabelecimento);
            estabelecimentoRepository.save(estabelecimento);
        }

        Comerciante comerciante = estabelecimento.getComerciante();
        Double novaMediaComerciante = avaliacaoRepository.findAverageByComercianteId(comerciante.getId());

        if (novaMediaComerciante != null) {
            comerciante.setMediaAvaliacoes(novaMediaComerciante);
            comercianteRepository.save(comerciante);
        }
    }
}
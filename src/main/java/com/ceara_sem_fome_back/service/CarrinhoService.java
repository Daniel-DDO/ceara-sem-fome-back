package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ItemCarrinhoRequestDTO;
import com.ceara_sem_fome_back.dto.ItemCarrinhoResponse;
import com.ceara_sem_fome_back.dto.CarrinhoResponse;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.CarrinhoRepository;
import com.ceara_sem_fome_back.repository.ProdutoCarrinhoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarrinhoService {

    @Autowired
    private final CarrinhoRepository carrinhoRepository;

    @Autowired
    private final BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private final ProdutoRepository produtoRepository;

    @Autowired
    private final ProdutoCarrinhoRepository produtoCarrinhoRepository;

    private final ConcurrentHashMap<String, Lock> produtoLocks = new ConcurrentHashMap<>();

    /**
     * Busca o carrinho do beneficiário.
     * @param beneficiarioEmail Email do usuário logado.
     * @return O carrinho do usuário.
     */
    @Transactional(readOnly = true)
    public CarrinhoResponse verMeuCarrinho(String beneficiarioEmail) {
        log.info("[SERVIÇO] Buscando carrinho para {}", beneficiarioEmail);

        Beneficiario beneficiario = beneficiarioRepository.findByEmail(beneficiarioEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiário não encontrado"));

        if (beneficiario.getCarrinho() == null) {
            return construirCarrinhoResponseVazio();
        }

        return construirCarrinhoResponse(beneficiario.getCarrinho());
    }

    /**
     * Adiciona um novo item ao carrinho do beneficiário.
     * @param beneficiarioEmail Email do usuário logado.
     * @param dto DTO com produtoId e quantidade.
     * @return O carrinho atualizado.
     */
    @Transactional
    public CarrinhoResponse adicionarItem(String beneficiarioEmail, ItemCarrinhoRequestDTO dto) {
        log.info("[SERVIÇO] Adicionando item {} (Qtd: {}) ao carrinho de {}",
                dto.getProdutoId(), dto.getQuantidade(), beneficiarioEmail);

        Lock produtoLock = produtoLocks.computeIfAbsent(dto.getProdutoId(), k -> new ReentrantLock());
        produtoLock.lock();

        try {
            Beneficiario beneficiario = beneficiarioRepository.findByEmail(beneficiarioEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiário não encontrado"));

            Carrinho carrinho = obterOuCriarCarrinho(beneficiario);
            Produto produto = produtoRepository.findById(dto.getProdutoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

            validarDisponibilidadeProduto(produto, dto.getQuantidade());

            // Usar método do model Carrinho para adicionar produto
            carrinho.adicionarProduto(produto, dto.getQuantidade());
            carrinhoRepository.save(carrinho);

            return construirCarrinhoResponse(carrinho);

        } finally {
            produtoLock.unlock();
            produtoLocks.remove(dto.getProdutoId(), produtoLock);
        }
    }

    /**
     * Atualiza a quantidade de um item no carrinho.
     * @param beneficiarioEmail Email do usuário logado.
     * @param produtoId ID do produto a ser atualizado.
     * @param dto DTO com a nova quantidade.
     * @return O carrinho atualizado.
     */
    @Transactional
    public CarrinhoResponse atualizarItem(String beneficiarioEmail, String produtoId, ItemCarrinhoRequestDTO dto) {
        log.info("[SERVIÇO] Atualizando item {} (Nova Qtd: {}) no carrinho de {}",
                produtoId, dto.getQuantidade(), beneficiarioEmail);

        if (dto.getQuantidade() <= 0) {
            return removerItem(beneficiarioEmail, produtoId);
        }

        Lock produtoLock = produtoLocks.computeIfAbsent(produtoId, k -> new ReentrantLock());
        produtoLock.lock();

        try {
            Beneficiario beneficiario = beneficiarioRepository.findByEmail(beneficiarioEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiário não encontrado"));

            Carrinho carrinho = beneficiario.getCarrinho();
            if (carrinho == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho não encontrado");
            }

            Produto produto = produtoRepository.findById(produtoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

            validarDisponibilidadeProduto(produto, dto.getQuantidade());

            // Encontrar e atualizar o item específico
            Optional<ProdutoCarrinho> itemOptional = produtoCarrinhoRepository
                    .findByCarrinhoIdAndProdutoId(carrinho.getId(), produtoId);

            if (itemOptional.isPresent()) {
                ProdutoCarrinho item = itemOptional.get();
                item.setQuantidade(dto.getQuantidade());
                produtoCarrinhoRepository.save(item);

                // Atualizar subtotal do carrinho
                carrinho.atualizarSubtotal();
                carrinhoRepository.save(carrinho);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado no carrinho");
            }

            return construirCarrinhoResponse(carrinho);

        } finally {
            produtoLock.unlock();
            produtoLocks.remove(produtoId, produtoLock);
        }
    }

    /**
     * Remove um item do carrinho.
     * @param beneficiarioEmail Email do usuário logado.
     * @param produtoId ID do produto a ser removido.
     * @return O carrinho atualizado.
     */
    @Transactional
    public CarrinhoResponse removerItem(String beneficiarioEmail, String produtoId) {
        log.info("[SERVIÇO] Removendo item {} do carrinho de {}", produtoId, beneficiarioEmail);

        Beneficiario beneficiario = beneficiarioRepository.findByEmail(beneficiarioEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiário não encontrado"));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho não encontrado");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        // Usar método do model Carrinho para remover produto
        carrinho.removerProduto(produto);
        carrinhoRepository.save(carrinho);

        return construirCarrinhoResponse(carrinho);
    }

    /**
     * Limpa todo o carrinho.
     * @param beneficiarioEmail Email do usuário logado.
     * @return O carrinho vazio.
     */
    @Transactional
    public CarrinhoResponse limparCarrinho(String beneficiarioEmail) {
        log.info("[SERVIÇO] Limpando carrinho de {}", beneficiarioEmail);

        Beneficiario beneficiario = beneficiarioRepository.findByEmail(beneficiarioEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beneficiário não encontrado"));

        Carrinho carrinho = beneficiario.getCarrinho();
        if (carrinho == null) {
            return construirCarrinhoResponseVazio();
        }

        // Usar método do model Carrinho para esvaziar
        carrinho.esvaziarCarrinho();
        carrinhoRepository.save(carrinho);

        return construirCarrinhoResponse(carrinho);
    }

    // Métodos auxiliares privados

    private Carrinho obterOuCriarCarrinho(Beneficiario beneficiario) {
        return Optional.ofNullable(beneficiario.getCarrinho())
                .orElseGet(() -> {
                    Carrinho novoCarrinho = new Carrinho();
                    Carrinho carrinhoSalvo = carrinhoRepository.save(novoCarrinho);
                    beneficiario.setCarrinho(carrinhoSalvo);
                    beneficiarioRepository.save(beneficiario);
                    return carrinhoSalvo;
                });
    }

    private void validarDisponibilidadeProduto(Produto produto, Integer quantidade) {
        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Estoque insuficiente para '%s'. Disponível: %d, Solicitado: %d",
                            produto.getNome(), produto.getQuantidadeEstoque(), quantidade));
        }

        if (produto.getStatus() != StatusProduto.AUTORIZADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Produto '%s' não está disponível para compra", produto.getNome()));
        }
    }

    private CarrinhoResponse construirCarrinhoResponse(Carrinho carrinho) {
        CarrinhoResponse response = new CarrinhoResponse();
        response.setId(carrinho.getId());
        response.setStatus(carrinho.getStatus().name());
        response.setCriacao(carrinho.getCriacao());
        response.setModificacao(carrinho.getModificacao());
        response.setSubtotal(carrinho.getSubtotal());

        var itens = carrinho.getProdutos().stream()
                .map(this::construirItemResponse)
                .toList();

        response.setItens(itens);
        return response;
    }

    private ItemCarrinhoResponse construirItemResponse(ProdutoCarrinho item) {
        Produto produto = item.getProduto();

        ItemCarrinhoResponse response = new ItemCarrinhoResponse();
        response.setId(item.getId());
        response.setProdutoId(produto.getId());
        response.setProdutoNome(produto.getNome());
        response.setLote(produto.getLote());
        response.setDescricao(produto.getDescricao());
        response.setQuantidade(item.getQuantidade());
        response.setPrecoUnitario(produto.getPreco());
        response.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        response.setQuantidadeEstoque(produto.getQuantidadeEstoque());
        response.setStatusProduto(produto.getStatus() != null ? produto.getStatus().name() : null);


        return response;
    }
    private CarrinhoResponse construirCarrinhoResponseVazio() {
        CarrinhoResponse response = new CarrinhoResponse();
        response.setSubtotal(BigDecimal.ZERO);
        response.setStatus("ABERTO");
        response.setCriacao(LocalDateTime.now());
        response.setModificacao(LocalDateTime.now());
        response.setItens(List.of());
        return response;
    }
}
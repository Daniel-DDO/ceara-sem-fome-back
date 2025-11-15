package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ItemCarrinhoRequestDTO;
import com.ceara_sem_fome_back.exception.EstoqueInsuficienteException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.CarrinhoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CarrinhoService {

    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;


    @Transactional
    public Carrinho verMeuCarrinho(String beneficiarioEmail) {
        log.info("[SERVIÇO] Buscando/Criando carrinho ativo para {}", beneficiarioEmail);

        Beneficiario beneficiario = beneficiarioRepository.findByEmail(beneficiarioEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado com e-mail: " + beneficiarioEmail));

        Carrinho carrinho = beneficiario.getCarrinho();

        // Verifica se o carrinho existe e está aberto. Se não, cria um novo.
        if (carrinho == null || carrinho.getStatus() != StatusCarrinho.ABERTO) {
            log.info("Carrinho não encontrado ou inativo. Criando novo carrinho para {}", beneficiarioEmail);
            carrinho = new Carrinho();
            carrinho.setBeneficiario(beneficiario);
            beneficiario.setCarrinho(carrinho); // Associa o novo carrinho ao beneficiário

            carrinho = carrinhoRepository.save(carrinho);
            log.info("Novo carrinho criado com ID: {}", carrinho.getId());
        }

        return carrinho;
    }

    @Transactional(readOnly = true)
    public List<Carrinho> buscarTodosCarrinhosAtivos() {
        log.info("[SERVIÇO] Buscando todos os carrinhos ativos (ABERTO).");
        return carrinhoRepository.findByStatus(StatusCarrinho.ABERTO);
    }



    @Transactional
    public Carrinho adicionarItem(String beneficiarioEmail, ItemCarrinhoRequestDTO dto) {
        log.info("[SERVIÇO] Adicionando item {} (Qtd: {}) ao carrinho de {}", dto.getProdutoId(), dto.getQuantidade(), beneficiarioEmail);

        // Garante que o carrinho esteja ativo
        Carrinho carrinho = verMeuCarrinho(beneficiarioEmail);

        // Busca o produto
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + dto.getProdutoId()));
        int quantidadeRequerida = dto.getQuantidade();
        
        // Verifica se o item ja esta no carrinho para somar a quantidade
        Optional<ProdutoCarrinho> itemExistente = carrinho.getProdutos().stream()
                .filter(item -> item.getProduto().getId().equals(produto.getId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            quantidadeRequerida += itemExistente.get().getQuantidade();
        }

        if (produto.getQuantidadeEstoque() < quantidadeRequerida) {
            throw new EstoqueInsuficienteException(
                String.format("Produto %s possui apenas %d unidades em estoque.", produto.getNome(), produto.getQuantidadeEstoque())
            );
        }

        carrinho.adicionarProduto(produto, dto.getQuantidade());

        return carrinhoRepository.save(carrinho);
    }

    // Atualiza a quantidade de um item no carrinho.
    @Transactional
    public Carrinho atualizarItem(String beneficiarioEmail, String produtoId, ItemCarrinhoRequestDTO dto) {
        log.info("[SERVIÇO] Atualizando item {} (Nova Qtd: {}) no carrinho de {}", produtoId, dto.getQuantidade(), beneficiarioEmail);

        // Garante que o carrinho esteja ativo
        Carrinho carrinho = verMeuCarrinho(beneficiarioEmail);

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + produtoId));

        if (produto.getQuantidadeEstoque() < dto.getQuantidade()) {
            throw new EstoqueInsuficienteException(
                String.format("Produto %s possui apenas %d unidades em estoque.", produto.getNome(), produto.getQuantidadeEstoque())
            );
        }

        // Busca o item no carrinho
        Optional<ProdutoCarrinho> itemOptional = carrinho.getProdutos().stream()
                .filter(item -> item.getProduto().getId().equals(produtoId))
                .findFirst();

       // Lógica para tratar item não encontrado
        if (itemOptional.isEmpty()) {
            // Se o item não está no carrinho E a quantidade é positiva,
            if (dto.getQuantidade() > 0) {

                log.warn("atualizarItem: Item {} nao encontrado, tratando como adicao.", produtoId);
                carrinho.adicionarProduto(produto, dto.getQuantidade());
                return carrinhoRepository.save(carrinho);
            }
            // Se o item não está no carrinho E a quantidade é zero,
            // não há nada a fazer. Apenas retorna o carrinho.
            return carrinho;
        }

        ProdutoCarrinho item = itemOptional.get();

        if (dto.getQuantidade() <= 0) {
            // Se a quantidade for zero, remove o item
            log.info("Quantidade <= 0. Removendo item do carrinho.");
            return removerItem(beneficiarioEmail, produtoId);
        }

        // Atualiza a quantidade e recalcula o subtotal
        item.setQuantidade(dto.getQuantidade());
        carrinho.atualizarSubtotal();

        return carrinhoRepository.save(carrinho);
    }


    @Transactional
    public Carrinho removerItem(String beneficiarioEmail, String produtoId) {
        log.info("[SERVIÇO] Removendo item {} do carrinho de {}", produtoId, beneficiarioEmail);

        // Garante que o carrinho esteja ativo
        Carrinho carrinho = verMeuCarrinho(beneficiarioEmail);

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + produtoId));
        

        carrinho.removerProduto(produto);

        return carrinhoRepository.save(carrinho);
    }

    @Transactional
    public Carrinho limparCarrinho(String beneficiarioEmail) {
        log.info("[SERVIÇO] Limpando e finalizando o carrinho do beneficiário: {}", beneficiarioEmail);

        // Busca o carrinho ativo diretamente pelo email e status
        Carrinho carrinho = carrinhoRepository.findByBeneficiarioEmailAndStatus(beneficiarioEmail, StatusCarrinho.ABERTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Não há carrinho ativo para limpar para o beneficiário: " + beneficiarioEmail));

        carrinho.esvaziarCarrinho();

        return carrinhoRepository.save(carrinho);
    }
}
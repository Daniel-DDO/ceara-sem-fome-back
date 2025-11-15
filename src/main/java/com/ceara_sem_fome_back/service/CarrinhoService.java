package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ItemCarrinhoRequestDTO;
import com.ceara_sem_fome_back.exception.EstoqueInsuficienteException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.CarrinhoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import com.ceara_sem_fome_back.repository.ProdutoCarrinhoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

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

    @Autowired
    private ProdutoCarrinhoRepository produtoCarrinhoRepository;


    @Transactional
    public Carrinho verMeuCarrinho(String beneficiarioEmail) {
        log.info("[SERVICO] Buscando/Criando carrinho ativo para {}", beneficiarioEmail);

        Beneficiario beneficiario = beneficiarioRepository.findByEmail(beneficiarioEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiario nao encontrado com e-mail: " + beneficiarioEmail));

        Carrinho carrinho = beneficiario.getCarrinho();

        if (carrinho == null || carrinho.getStatus() != StatusCarrinho.ABERTO) {
            log.info("Carrinho nao encontrado ou inativo. Criando novo carrinho para {}", beneficiarioEmail);
            carrinho = new Carrinho();
            carrinho.setBeneficiario(beneficiario);
            beneficiario.setCarrinho(carrinho); 
            carrinho = carrinhoRepository.save(carrinho);
            log.info("Novo carrinho criado com ID: {}", carrinho.getId());
        }

        return carrinho;
    }

    @Transactional(readOnly = true)
    public List<Carrinho> buscarTodosCarrinhosAtivos() {
        log.info("[SERVICO] Buscando todos os carrinhos ativos (ABERTO).");
        return carrinhoRepository.findByStatus(StatusCarrinho.ABERTO);
    }

    @Transactional
    public Carrinho adicionarItem(String beneficiarioEmail, ItemCarrinhoRequestDTO dto) {
        log.info("[SERVICO] Adicionando item {} (Qtd: {}) ao carrinho de {}", dto.getProdutoId(), dto.getQuantidade(), beneficiarioEmail);

        Carrinho carrinho = verMeuCarrinho(beneficiarioEmail);
        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + dto.getProdutoId()));

        // Procura se o item JA EXISTE no repositorio
        Optional<ProdutoCarrinho> itemExistenteOpt = produtoCarrinhoRepository.findByCarrinhoIdAndProdutoId(carrinho.getId(), produto.getId());

        int quantidadeRequerida = dto.getQuantidade(); // Declaracao UNICA
        ProdutoCarrinho itemParaSalvar;

        if (itemExistenteOpt.isPresent()) {
            // Se ja existe, atualiza a quantidade
            itemParaSalvar = itemExistenteOpt.get();
            quantidadeRequerida += itemParaSalvar.getQuantidade();
        } else {
            // Se nao existe, cria um novo
            itemParaSalvar = new ProdutoCarrinho();
            itemParaSalvar.setCarrinho(carrinho);
            itemParaSalvar.setProduto(produto);
        }

        // VERIFICACAO DE ESTOQUE
        if (produto.getQuantidadeEstoque() < quantidadeRequerida) {
            throw new EstoqueInsuficienteException(
                String.format("Produto %s possui apenas %d unidades em estoque.", produto.getNome(), produto.getQuantidadeEstoque())
            );
        }
        
        itemParaSalvar.setQuantidade(quantidadeRequerida);
        produtoCarrinhoRepository.save(itemParaSalvar); // Salva o item

        // Adiciona a lista do carrinho (se nao estiver la)
        if (itemExistenteOpt.isEmpty()) {
             carrinho.getProdutos().add(itemParaSalvar);
        }
        
        carrinho.atualizarSubtotal();
        return carrinhoRepository.save(carrinho); // Salva o carrinho
    }

    @Transactional
    public Carrinho atualizarItem(String beneficiarioEmail, String produtoId, ItemCarrinhoRequestDTO dto) {
        log.info("[SERVICO] Atualizando item {} (Nova Qtd: {}) no carrinho de {}", produtoId, dto.getQuantidade(), beneficiarioEmail);

        Carrinho carrinho = verMeuCarrinho(beneficiarioEmail);
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com ID: " + produtoId));

        // Validacao de estoque (correta)
        if (produto.getQuantidadeEstoque() < dto.getQuantidade()) {
            throw new EstoqueInsuficienteException(
                String.format("Produto %s possui apenas %d unidades em estoque.", produto.getNome(), produto.getQuantidadeEstoque())
            );
        }

        // Buscar item no repositorio, nao no cache
        Optional<ProdutoCarrinho> itemOptional = produtoCarrinhoRepository.findByCarrinhoIdAndProdutoId(carrinho.getId(), produtoId);

        if (dto.getQuantidade() <= 0) {
            // Se a quantidade for zero, remove o item (se existir)
            if (itemOptional.isPresent()) {
                log.info("Quantidade <= 0. Removendo item do carrinho.");
                // Chama o metodo removerItem que ja tem a logica correta
                return removerItem(beneficiarioEmail, produtoId);
            } else {
                // Se nao existe e a qtd e 0, nao faz nada
                return carrinho;
            }
        }

        // A partir daqui, dto.getQuantidade() > 0
        ProdutoCarrinho itemParaSalvar;
        if (itemOptional.isEmpty()) {
            // Se nao existe, cria um novo
            log.warn("atualizarItem: Item {} nao encontrado, tratando como adicao.", produtoId);
            itemParaSalvar = new ProdutoCarrinho();
            itemParaSalvar.setCarrinho(carrinho);
            itemParaSalvar.setProduto(produto);
            carrinho.getProdutos().add(itemParaSalvar); // Adiciona na colecao em memoria
        } else {
            // Se existe, pega a referencia
            itemParaSalvar = itemOptional.get();
        }

        // Atualiza a quantidade (seja novo ou existente)
        itemParaSalvar.setQuantidade(dto.getQuantidade());
        produtoCarrinhoRepository.save(itemParaSalvar); // Salva o item (novo ou atualizado)

        carrinho.atualizarSubtotal();
        return carrinhoRepository.save(carrinho); // Salva o carrinho (subtotal)
    }

    @Transactional
    public Carrinho removerItem(String beneficiarioEmail, String produtoId) {
        log.info("[SERVICO] Removendo item {} do carrinho de {}", produtoId, beneficiarioEmail);

        Carrinho carrinho = verMeuCarrinho(beneficiarioEmail);

        // Deleta o item direto do repositorio pela chave
        produtoCarrinhoRepository.deleteByCarrinhoIdAndProdutoId(carrinho.getId(), produtoId);

        // Remove da colecao em memoria
        carrinho.getProdutos().removeIf(item -> {
            // Evita NullPointerException se o getProduto() for nulo na colecao
            if (item.getProduto() == null) return false; 
            return item.getProduto().getId().equals(produtoId);
        });

        carrinho.atualizarSubtotal();
        return carrinhoRepository.save(carrinho);
    }

    @Transactional
    public Carrinho limparCarrinho(String beneficiarioEmail) {
        log.info("[SERVICO] Limpando e finalizando o carrinho do beneficiario: {}", beneficiarioEmail);

        Carrinho carrinho = carrinhoRepository.findByBeneficiarioEmailAndStatus(beneficiarioEmail, StatusCarrinho.ABERTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nao ha carrinho ativo para limpar para o beneficiario: " + beneficiarioEmail));

        // Pega os IDs dos produtos no carrinho
        List<String> idsParaDeletar = carrinho.getProdutos().stream()
                .map(ProdutoCarrinho::getId)
                .collect(Collectors.toList());
        
        // Deleta todos os ProdutoCarrinho associados
        if (!idsParaDeletar.isEmpty()) {
            produtoCarrinhoRepository.deleteAllById(idsParaDeletar);
        }

        // Limpa a colecao em memoria e atualiza o status/subtotal
        carrinho.esvaziarCarrinho();

        return carrinhoRepository.save(carrinho);
    }
}
package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ItemCarrinhoRequestDTO;
import com.ceara_sem_fome_back.model.Carrinho;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.CarrinhoRepository;
import com.ceara_sem_fome_back.repository.ProdutoCarrinhoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * [PLACEHOLDER] Busca ou cria o carrinho do beneficiário.
     * @param beneficiarioEmail Email do usuário logado.
     * @return O carrinho do usuário.
     */
    @Transactional(readOnly = true)
    public Carrinho verMeuCarrinho(String beneficiarioEmail) {
        log.info("[SERVIÇO] Buscando carrinho para {}", beneficiarioEmail);

        
        return new Carrinho();
    }


    /**
     * [PLACEHOLDER] Adiciona um novo item ao carrinho do beneficiário.
     * @param beneficiarioEmail Email do usuário logado.
     * @param dto DTO com produtoId e quantidade.
     * @return O carrinho atualizado.
     */
    @Transactional
    public Carrinho adicionarItem(String beneficiarioEmail, ItemCarrinhoRequestDTO dto) {
        log.info("[SERVIÇO] Adicionando item {} (Qtd: {}) ao carrinho de {}", dto.getProdutoId(), dto.getQuantidade(), beneficiarioEmail);
 
        return new Carrinho();
    }

    /**
     * [PLACEHOLDER] Atualiza a quantidade de um item no carrinho.
     * @param beneficiarioEmail Email do usuário logado.
     * @param produtoId ID do produto a ser atualizado.
     * @param dto DTO com a nova quantidade.
     * @return O carrinho atualizado.
     */
    @Transactional
    public Carrinho atualizarItem(String beneficiarioEmail, String produtoId, ItemCarrinhoRequestDTO dto) {
        log.info("[SERVIÇO] Atualizando item {} (Nova Qtd: {}) no carrinho de {}", produtoId, dto.getQuantidade(), beneficiarioEmail);

        return new Carrinho();
    }

    /**
     * [PLACEHOLDER] Remove um item do carrinho.
     * @param beneficiarioEmail Email do usuário logado.
     * @param produtoId ID do produto a ser removido.
     * @return O carrinho atualizado.
     */
    @Transactional
    public Carrinho removerItem(String beneficiarioEmail, String produtoId) {
        log.info("[SERVIÇO] Removendo item {} do carrinho de {}", produtoId, beneficiarioEmail);

        return new Carrinho(); 
    }
}
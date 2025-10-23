package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ProdutoCadastroRequest;
import com.ceara_sem_fome_back.exception.AcessoNaoAutorizadoException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoEstabelecimentoRepository;
import com.ceara_sem_fome_back.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import com.ceara_sem_fome_back.model.Produto;

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

    public ProdutoEstabelecimento cadastrarProduto(ProdutoCadastroRequest request, Comerciante comerciante) {

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(request.getEstabelecimentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(request.getEstabelecimentoId()));

        //Validação de Propriedade
        if (!estabelecimento.getComerciante().getCpf().equals(comerciante.getCpf())) {
            throw new AcessoNaoAutorizadoException(comerciante.getCpf());
        }

        //Cria e salva o Produto
        Produto novoProduto = new Produto();
        novoProduto.setId(UUID.randomUUID().toString());
        novoProduto.setNome(request.getNome());
        novoProduto.setCriador(comerciante);

        Produto produtoSalvo = produtoRepository.save(novoProduto);


        ProdutoEstabelecimentoId id = new ProdutoEstabelecimentoId(produtoSalvo.getId(), estabelecimento.getId());
        ProdutoEstabelecimento associacao = new ProdutoEstabelecimento();
        associacao.setId(id);
        associacao.setProduto(produtoSalvo);
        associacao.setEstabelecimento(estabelecimento);
        associacao.setPrecoVenda(request.getPrecoVenda());
        associacao.setEstoque(request.getEstoque());

        produtoSalvo.getEstabelecimentos().add(associacao);
        estabelecimento.getProdutos().add(associacao);

        return produtoEstabelecimentoRepository.save(associacao);
    }
    public List<ProdutoEstabelecimento> listarProdutosPorEstabelecimento(String estabelecimentoId) {

        List<ProdutoEstabelecimento> produtos = produtoEstabelecimentoRepository.findByEstabelecimento_Id(estabelecimentoId);
        return produtos;
    }

    //apenas o começo, vai mudar

    public void aprovarProduto(Produto produto) {

    }

    public void recusarProduto(Produto produto) {

    }

    public void editarProduto(Produto produto) {

    }

    public void removerProduto(Produto produto) {

    }

}


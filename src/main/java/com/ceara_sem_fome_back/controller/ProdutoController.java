package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.ProdutoCadastroRequest;
import com.ceara_sem_fome_back.dto.ProdutoDTO;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Produto;
import com.ceara_sem_fome_back.model.ProdutoEstabelecimento;
import com.ceara_sem_fome_back.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping("/cadastrar")
    public ResponseEntity<ProdutoEstabelecimento> cadastrarProduto(
            @RequestBody @Valid ProdutoCadastroRequest request,
            @AuthenticationPrincipal ComercianteData comercianteData) {

        Comerciante comercianteLogado = comercianteData.getComerciante();

        ProdutoEstabelecimento associacaoSalva = produtoService.cadastrarProduto(request, comercianteLogado);

        return ResponseEntity.status(201).body(associacaoSalva);
    }

    @GetMapping("/estabelecimento/{estabelecimentoId}")
    public ResponseEntity<List<ProdutoEstabelecimento>> listarProdutosPorEstabelecimento(
            @PathVariable String estabelecimentoId) {

        List<ProdutoEstabelecimento> produtos = produtoService.listarProdutosPorEstabelecimento(estabelecimentoId);
        return ResponseEntity.ok(produtos);
    }

    @PostMapping("/aprovar")
    public ResponseEntity<ProdutoDTO> aprovarProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoAprov = produtoService.aprovarProduto(id);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoAprov.getId(), produtoAprov.getNome(), produtoAprov.getLote(),
                produtoAprov.getDescricao(), produtoAprov.getPreco(), produtoAprov.getQuantidadeEstoque(), produtoAprov.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @PostMapping("/recusar")
    public ResponseEntity<ProdutoDTO> recusarProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoRec = produtoService.recusarProduto(id);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoRec.getId(), produtoRec.getNome(), produtoRec.getLote(),
                produtoRec.getDescricao(), produtoRec.getPreco(), produtoRec.getQuantidadeEstoque(), produtoRec.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @PostMapping("/editar")
    public ResponseEntity<ProdutoDTO> editarProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoEdit = produtoService.editarProduto(id, produtoDTO.getNome(), produtoDTO.getLote(),
                produtoDTO.getDescricao(), produtoDTO.getPreco(), produtoDTO.getQuantidadeEstoque());

        ProdutoDTO prodRespota = new ProdutoDTO(produtoEdit.getId(), produtoEdit.getNome(), produtoEdit.getLote(),
                produtoEdit.getDescricao(), produtoEdit.getPreco(), produtoEdit.getQuantidadeEstoque(), produtoEdit.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @PostMapping("/remover")
    public ResponseEntity<ProdutoDTO> removerProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoRemov = produtoService.removerProduto(id);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoRemov.getId(), produtoRemov.getNome(), produtoRemov.getLote(),
                produtoRemov.getDescricao(), produtoRemov.getPreco(), produtoRemov.getQuantidadeEstoque(), produtoRemov.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

}
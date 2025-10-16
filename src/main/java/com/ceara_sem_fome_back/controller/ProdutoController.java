package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.ProdutoCadastroRequest;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.ProdutoEstabelecimento;
import com.ceara_sem_fome_back.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
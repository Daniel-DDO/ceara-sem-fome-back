package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.dto.CarrinhoResponse;
import com.ceara_sem_fome_back.dto.ItemCarrinhoRequestDTO;
import com.ceara_sem_fome_back.service.CarrinhoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrinho")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    @GetMapping("/meu-carrinho")
    public ResponseEntity<CarrinhoResponse> verMeuCarrinho(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData) {
        
        String email = beneficiarioData.getUsername();
        CarrinhoResponse carrinho = carrinhoService.verMeuCarrinho(email);
        return ResponseEntity.ok(carrinho);
    }

    @PostMapping("/item")
    public ResponseEntity<CarrinhoResponse> adicionarItem(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData,
            @Valid @RequestBody ItemCarrinhoRequestDTO dto) {

        String email = beneficiarioData.getUsername();
        CarrinhoResponse carrinho = carrinhoService.adicionarItem(email, dto);
        return ResponseEntity.status(201).body(carrinho);
    }

    @PutMapping("/item/{produtoId}")
    public ResponseEntity<CarrinhoResponse> atualizarItem(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData,
            @PathVariable String produtoId,
            @Valid @RequestBody ItemCarrinhoRequestDTO dto) {

        String email = beneficiarioData.getUsername();
        // A lógica de serviço deve garantir que dto.getProdutoId() seja igual a produtoId,
        // ou simplesmente ignorar o dto.getProdutoId() e usar o da URL.
        CarrinhoResponse carrinho = carrinhoService.atualizarItem(email, produtoId, dto);
        return ResponseEntity.ok(carrinho);
    }

    @DeleteMapping("/item/{produtoId}")
    public ResponseEntity<CarrinhoResponse> removerItem(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData,
            @PathVariable String produtoId) {
        
        String email = beneficiarioData.getUsername();
        CarrinhoResponse carrinho = carrinhoService.removerItem(email, produtoId);
        return ResponseEntity.ok(carrinho);
    }


    @DeleteMapping("/limpar")
    public ResponseEntity<CarrinhoResponse> limparCarrinho(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData) {

        String email = beneficiarioData.getUsername();
        CarrinhoResponse carrinho = carrinhoService.limparCarrinho(email);
        return ResponseEntity.ok(carrinho);
    }

}
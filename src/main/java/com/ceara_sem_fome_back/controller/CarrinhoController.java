package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.dto.ItemCarrinhoRequestDTO;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.Carrinho;
import com.ceara_sem_fome_back.service.CarrinhoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carrinho")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    @GetMapping("/meu-carrinho")
    public ResponseEntity<?> verMeuCarrinho(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData) {
        try {
            String email = beneficiarioData.getUsername();
            Carrinho carrinho = carrinhoService.verMeuCarrinho(email);
            return ResponseEntity.ok(carrinho);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (RuntimeException e) {
            // Captura outros erros de negócio
            return ResponseEntity.badRequest().body("Erro ao buscar carrinho: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro interno ao tentar obter carrinho.");
        }
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Carrinho>> listarTodosCarrinhosAtivos() {
        List<Carrinho> carrinhosAtivos = carrinhoService.buscarTodosCarrinhosAtivos();
        return ResponseEntity.ok(carrinhosAtivos);
    }

    @PostMapping("/item")
    public ResponseEntity<?> adicionarItem(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData,
            @Valid @RequestBody ItemCarrinhoRequestDTO dto) {
        try {
            String email = beneficiarioData.getUsername();
            Carrinho carrinho = carrinhoService.adicionarItem(email, dto);
            return ResponseEntity.status(201).body(carrinho);
        } catch (RecursoNaoEncontradoException e) {
            // Produto ou Beneficiário não encontrado
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (RuntimeException e) {
            // Captura outros erros de negócio
            return ResponseEntity.badRequest().body("Erro ao adicionar item: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro interno ao tentar adicionar item.");
        }
    }

    @PutMapping("/item/{produtoId}")
    public ResponseEntity<?> atualizarItem(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData,
            @PathVariable String produtoId,
            @Valid @RequestBody ItemCarrinhoRequestDTO dto) {
        try {
            String email = beneficiarioData.getUsername();
            Carrinho carrinho = carrinhoService.atualizarItem(email, produtoId, dto);
            return ResponseEntity.ok(carrinho);
        } catch (RecursoNaoEncontradoException e) {
            // Item não encontrado no carrinho
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (RuntimeException e) {
            // Captura outros erros de negócio
            return ResponseEntity.badRequest().body("Erro ao atualizar item: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro interno ao tentar atualizar item.");
        }
    }

    @DeleteMapping("/item/{produtoEstabelecimentoId}")
    public ResponseEntity<?> removerItem(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData,
            @PathVariable String produtoEstabelecimentoId) {
        try {
            String email = beneficiarioData.getUsername();
            Carrinho carrinho = carrinhoService.removerItem(email, produtoEstabelecimentoId);
            return ResponseEntity.ok(carrinho);
        } catch (RecursoNaoEncontradoException e) {
            // Produto não encontrado
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (RuntimeException e) {
            // Captura outros erros de negócio
            return ResponseEntity.badRequest().body("Erro ao remover item: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro interno ao tentar remover item.");
        }
    }

    @DeleteMapping("/meu-carrinho")
    public ResponseEntity<?> limparMeuCarrinho(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData) {
        try {
            String email = beneficiarioData.getUsername();
            Carrinho carrinhoLimpo = carrinhoService.limparCarrinho(email);
            // Retorna o carrinho com a lista de produtos vazia e status FINALIZADO
            return ResponseEntity.ok(carrinhoLimpo);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro ao limpar carrinho: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro interno ao tentar limpar carrinho.");
        }
    }
}
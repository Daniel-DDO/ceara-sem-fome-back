package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.model.Compra;
import com.ceara_sem_fome_back.model.ItemCompra;
import com.ceara_sem_fome_back.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compra")
@CrossOrigin(origins = "*")
public class CompraController {

    @Autowired
    private CompraService compraService;

    /**
     * Finaliza uma compra com base no carrinho do beneficiário.
     * Agora o ID do estabelecimento também é obrigatório.
     */
    @PostMapping("/finalizar/{beneficiarioId}/{estabelecimentoId}")
    public ResponseEntity<?> finalizarCompra(
            @PathVariable String beneficiarioId,
            @PathVariable String estabelecimentoId) {

        try {
            Compra compra = compraService.finalizarCompra(beneficiarioId, estabelecimentoId);
            return ResponseEntity.ok(compra);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro inesperado ao finalizar compra.");
        }
    }

    /**
     * Retorna todas as compras realizadas.
     */
    @GetMapping
    public ResponseEntity<List<Compra>> listarTodas() {
        List<Compra> compras = compraService.listarTodas();
        return ResponseEntity.ok(compras);
    }

    /**
     * Retorna todas as compras de um beneficiário específico.
     */
    @GetMapping("/beneficiario/{beneficiarioId}")
    public ResponseEntity<?> listarPorBeneficiario(@PathVariable String beneficiarioId) {
        try {
            List<Compra> compras = compraService.listarPorBeneficiario(beneficiarioId);
            return ResponseEntity.ok(compras);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retorna os itens de uma compra específica.
     */
    @GetMapping("/{compraId}/itens")
    public ResponseEntity<?> listarItensDaCompra(@PathVariable String compraId) {
        try {
            List<ItemCompra> itens = compraService.listarItensDaCompra(compraId);
            return ResponseEntity.ok(itens);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Gera um comprovante (nota fiscal simples) em formato texto.
     */
    @GetMapping("/{compraId}/comprovante")
    public ResponseEntity<?> gerarComprovante(@PathVariable String compraId) {
        try {
            String comprovante = compraService.gerarComprovante(compraId);
            return ResponseEntity.ok(comprovante);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}

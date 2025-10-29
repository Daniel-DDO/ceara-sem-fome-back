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
     * Cria uma nova compra a partir do carrinho do beneficiário
     */
    @PostMapping("/finalizar/{beneficiarioId}")
    public ResponseEntity<Compra> finalizarCompra(@PathVariable String beneficiarioId) {
        try {
            Compra compra = compraService.finalizarCompra(beneficiarioId);
            return ResponseEntity.ok(compra);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retorna todas as compras realizadas
     */
    @GetMapping
    public ResponseEntity<List<Compra>> listarTodas() {
        List<Compra> compras = compraService.listarTodas();
        return ResponseEntity.ok(compras);
    }

    /**
     * Retorna todas as compras de um beneficiário específico
     */
    @GetMapping("/beneficiario/{beneficiarioId}")
    public ResponseEntity<List<Compra>> listarPorBeneficiario(@PathVariable String beneficiarioId) {
        List<Compra> compras = compraService.listarPorBeneficiario(beneficiarioId);
        return ResponseEntity.ok(compras);
    }

    /**
     * Retorna os itens de uma compra específica
     */
    @GetMapping("/{compraId}/itens")
    public ResponseEntity<List<ItemCompra>> listarItensDaCompra(@PathVariable String compraId) {
        List<ItemCompra> itens = compraService.listarItensDaCompra(compraId);
        return ResponseEntity.ok(itens);
    }

    /**
     * Gera um comprovante (nota fiscal simples) em formato texto
     */
    @GetMapping("/{compraId}/comprovante")
    public ResponseEntity<String> gerarComprovante(@PathVariable String compraId) {
        try {
            String comprovante = compraService.gerarComprovante(compraId);
            return ResponseEntity.ok(comprovante);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao gerar comprovante: " + e.getMessage());
        }
    }
}

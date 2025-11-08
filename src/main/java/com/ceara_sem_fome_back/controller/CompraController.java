package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.ReciboDTO;
import com.ceara_sem_fome_back.model.Compra;
import com.ceara_sem_fome_back.model.ItemCompra;
import com.ceara_sem_fome_back.service.CompraService;
import com.ceara_sem_fome_back.service.ReciboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compra")
@CrossOrigin(origins = "*")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Autowired
    private ReciboService reciboService;

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

    @GetMapping
    public ResponseEntity<List<Compra>> listarTodas() {
        List<Compra> compras = compraService.listarTodas();
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/beneficiario/{beneficiarioId}")
    public ResponseEntity<?> listarPorBeneficiario(@PathVariable String beneficiarioId) {
        try {
            List<Compra> compras = compraService.listarPorBeneficiario(beneficiarioId);
            return ResponseEntity.ok(compras);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{compraId}/itens")
    public ResponseEntity<?> listarItensDaCompra(@PathVariable String compraId) {
        try {
            List<ItemCompra> itens = compraService.listarItensDaCompra(compraId);
            return ResponseEntity.ok(itens);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{compraId}/comprovante")
    public ResponseEntity<?> gerarComprovante(@PathVariable String compraId) {
        try {
            String comprovante = compraService.gerarComprovante(compraId);
            return ResponseEntity.ok(comprovante);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/{compraId}/recibo-pdf")
    public ResponseEntity<byte[]> gerarReciboPDF(@PathVariable String compraId) {
        try {
            byte[] recibo = reciboService.gerarReciboPDF(compraId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "recibo_" + compraId + ".pdf");
            return new ResponseEntity<>(recibo, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{compraId}/recibo-dados")
    public ResponseEntity<ReciboDTO> obterReciboDados(@PathVariable String compraId) {
        try {
            ReciboDTO reciboDTO = compraService.obterReciboDTO(compraId);
            return ResponseEntity.ok(reciboDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

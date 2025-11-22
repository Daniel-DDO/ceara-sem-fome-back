package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.CompraDTO;
import com.ceara_sem_fome_back.dto.HistoricoVendasDTO;
import com.ceara_sem_fome_back.dto.ReciboDTO;
import com.ceara_sem_fome_back.model.Compra;
import com.ceara_sem_fome_back.model.ProdutoCompra;
import com.ceara_sem_fome_back.model.StatusCompra;
import com.ceara_sem_fome_back.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compra")
@RequiredArgsConstructor
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping("/finalizar/{beneficiarioId}")
    public ResponseEntity<List<Compra>> finalizarCompra(@PathVariable String beneficiarioId) {
        List<Compra> compras = compraService.finalizarCompra(beneficiarioId);
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/beneficiario/{beneficiarioId}")
    public ResponseEntity<List<CompraDTO>> listarComprasPorBeneficiario(@PathVariable String beneficiarioId) {
        List<CompraDTO> compras = compraService.listarComprasBeneficiario(beneficiarioId);
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CompraDTO>> listarTodas() {
        List<CompraDTO> compras = compraService.listarTodas();
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/comerciante/{comercianteId}")
    public ResponseEntity<List<CompraDTO>> listarComprasDoComerciante(@PathVariable String comercianteId) {
        List<CompraDTO> compras = compraService.listarComprasDoComerciante(comercianteId);
        return ResponseEntity.ok(compras);
    }


    @GetMapping("/{compraId}")
    public ResponseEntity<Compra> obterCompra(@PathVariable String compraId) {
        Compra compra = compraService.obterCompraPorId(compraId);
        return ResponseEntity.ok(compra);
    }

    @PutMapping("/{compraId}/status")
    public ResponseEntity<Compra> atualizarStatus(@PathVariable String compraId, @RequestParam StatusCompra status) {
        Compra compra = compraService.atualizarStatusCompra(compraId, status);
        return ResponseEntity.ok(compra);
    }

    @PostMapping("/{compraId}/retirada")
    public ResponseEntity<Compra> marcarComoRetirada(@PathVariable String compraId) {
        Compra compra = compraService.marcarComoRetirada(compraId);
        return ResponseEntity.ok(compra);
    }

    @PostMapping("/{compraId}/entregue")
    public ResponseEntity<Compra> marcarComoEntregue(@PathVariable String compraId) {
        Compra compra = compraService.marcarComoEntregue(compraId);
        return ResponseEntity.ok(compra);
    }

    @PostMapping("/{compraId}/reembolso")
    public ResponseEntity<Compra> reembolsarCompra(@PathVariable String compraId) {
        Compra compra = compraService.reembolsarCompra(compraId);
        return ResponseEntity.ok(compra);
    }

    @GetMapping("/{compraId}/itens")
    public ResponseEntity<List<ProdutoCompra>> listarItensDaCompra(@PathVariable String compraId) {
        List<ProdutoCompra> itens = compraService.listarItensDaCompra(compraId);
        return ResponseEntity.ok(itens);
    }

    @GetMapping("/recibos/{beneficiarioId}")
    public ResponseEntity<List<ReciboDTO>> gerarRecibos(@PathVariable String beneficiarioId) {
        List<ReciboDTO> recibos = compraService.gerarRecibosPorComprao(beneficiarioId);
        return ResponseEntity.ok(recibos);
    }

    @GetMapping("/vendas/{estabelecimentoId}")
    public ResponseEntity<List<HistoricoVendasDTO>> listarVendas(@PathVariable String estabelecimentoId) {
        List<HistoricoVendasDTO> vendas = compraService.listarVendasEstabelecimento(estabelecimentoId);
        return ResponseEntity.ok(vendas);
    }

}

package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.data.EntregadorData;
import com.ceara_sem_fome_back.exception.AcessoNaoAutorizadoException;
import com.ceara_sem_fome_back.model.Compra;
import com.ceara_sem_fome_back.model.EventoCompra;
import com.ceara_sem_fome_back.model.StatusCompra;
import com.ceara_sem_fome_back.repository.CompraRepository;
import com.ceara_sem_fome_back.service.EstabelecimentoService;
import com.ceara_sem_fome_back.service.EventoCompraService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compra")
@CrossOrigin(origins = "*")
public class CompraEventoController {

    @Autowired
    private EventoCompraService eventoCompraService;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private EstabelecimentoService estabelecimentoService;

    @GetMapping("/{compraId}/eventos")
    public ResponseEntity<?> listarEventosDaCompra(@PathVariable String compraId) {
        try {
            List<EventoCompra> eventos = eventoCompraService.listarEventosPorCompra(compraId);
            return ResponseEntity.ok(eventos);
        } catch (RuntimeException e) {
            // Captura a excecao "Compra nao encontrada" do service
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Getter
    @Setter
    public static class StatusUpdateRequest {
        private StatusCompra novoStatus;
        private String descricao;
    }

    @PatchMapping("/{compraId}/status")
    public ResponseEntity<?> atualizarStatusCompra(
            @PathVariable String compraId,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {

        try {
            //Busca a compra
            Compra compra = compraRepository.findById(compraId)
                    .orElseThrow(() -> new RuntimeException("Compra nao encontrada com ID: " + compraId));
            Object principal = authentication.getPrincipal();
            StatusCompra novoStatus = request.getNovoStatus();

            if (principal instanceof ComercianteData comercianteData) {
                //Comerciante so pode preparar ou sinalizar pronto
                if (novoStatus != StatusCompra.EM_PREPARACAO && novoStatus != StatusCompra.PRONTO_PARA_ENTREGA) {
                    throw new AcessoNaoAutorizadoException("Comerciante so pode alterar status para EM_PREPARACAO ou PRONTO_PARA_ENTREGA.");
                }

                //Comerciante so pode alterar pedidos do seu estabelecimento
                String comercianteId = comercianteData.getComerciante().getId();
                String estabelecimentoIdDaCompra = compra.getEstabelecimento().getId();
                
                //service que ja existe para verificar a propriedade
                estabelecimentoService.verificarPropriedade(estabelecimentoIdDaCompra, comercianteId);

            } else if (principal instanceof EntregadorData) {
                //Entregador so pode alterar para A_CAMINHO ou ENTREGUE
                if (novoStatus != StatusCompra.A_CAMINHO && novoStatus != StatusCompra.ENTREGUE) {
                    throw new AcessoNaoAutorizadoException("Entregador so pode alterar status para A_CAMINHO ou ENTREGUE.");
                }

            
            } else if (principal instanceof BeneficiarioData) {
                 //Beneficiario NAO PODE alterar o status
                throw new AcessoNaoAutorizadoException("Beneficiario nao pode alterar o status do pedido.");
            
            } else {
            }
            EventoCompra evento = eventoCompraService.criarEvento(
                    compra,
                    request.getNovoStatus(),
                    request.getDescricao()
            );

            return ResponseEntity.ok(evento);

        } catch (AcessoNaoAutorizadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}
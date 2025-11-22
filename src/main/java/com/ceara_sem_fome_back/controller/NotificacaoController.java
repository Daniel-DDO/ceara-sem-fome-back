package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.dto.NotificacaoResponseDTO;
import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notificacoes")
@CrossOrigin(origins = {
        "https://ceara-raiz-srb9k.ondigitalocean.app",
        "http://localhost:8080",
        "http://localhost:5173",
        "https://*.cloudworkstations.dev"
})
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listarMinhasNotificacoes(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData) {

        if (beneficiarioData == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String usuarioId = beneficiarioData.getId();
        List<Notificacao> notificacoes = notificacaoService.listarPorUsuario(usuarioId);

        List<NotificacaoResponseDTO> dtos = notificacoes.stream()
                .map(n -> new NotificacaoResponseDTO(
                        n.getId(),
                        n.getMensagem(),
                        n.getDataCriacao(),
                        n.isLida()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nao-lidas/count")
    public ResponseEntity<Long> contarNaoLidas(@AuthenticationPrincipal BeneficiarioData beneficiarioData) {
        if (beneficiarioData == null) return ResponseEntity.status(401).build();

        String usuarioId = beneficiarioData.getId();
        long count = notificacaoService.listarPorUsuario(usuarioId).stream()
                .filter(n -> !n.isLida())
                .count();

        return ResponseEntity.ok(count);
    }

    public record NotificacaoTesteRequest(String destinatarioId, String mensagem) {}

    @PostMapping("/teste-envio")
    public ResponseEntity<Void> enviarNotificacaoTeste(@RequestBody NotificacaoTesteRequest request) {
        notificacaoService.criarEEnviarNotificacao(request.destinatarioId(), request.mensagem());
        return ResponseEntity.ok().build();
    }
}
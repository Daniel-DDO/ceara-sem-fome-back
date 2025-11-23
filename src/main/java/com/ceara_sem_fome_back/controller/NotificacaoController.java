package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.data.EntregadorData;
import com.ceara_sem_fome_back.dto.NotificacaoResponseDTO;
import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<List<NotificacaoResponseDTO>> listarMinhasNotificacoes(Authentication authentication) {
        String usuarioId = extrairIdUsuario(authentication);

        List<Notificacao> notificacoes = notificacaoService.listarPorUsuario(usuarioId);

        List<NotificacaoResponseDTO> dtos = notificacoes.stream()
                .map(NotificacaoResponseDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/nao-lidas/count")
    public ResponseEntity<Long> contarNaoLidas(Authentication authentication) {
        String usuarioId = extrairIdUsuario(authentication);
        long count = notificacaoService.contarNaoLidas(usuarioId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.ok().build();
    }

    // Novo endpoint útil: Marcar tudo como lido
    @PatchMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(Authentication authentication) {
        String usuarioId = extrairIdUsuario(authentication);
        notificacaoService.marcarTodasComoLidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    private String extrairIdUsuario(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof BeneficiarioData b) return b.getId();
        if (principal instanceof ComercianteData c) return c.getComerciante().getId();
        if (principal instanceof AdministradorData a) return a.getAdministrador().getId();
        if (principal instanceof EntregadorData e) return e.getEntregador().getId();

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de usuário desconhecido");
    }
}
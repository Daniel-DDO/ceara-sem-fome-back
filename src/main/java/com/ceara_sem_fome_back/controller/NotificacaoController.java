package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.NotificacaoResponseDTO;
import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listarMinhasNotificacoes(Authentication authentication) {
        try {
            String usuarioId = extrairIdUsuario(authentication);
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

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao listar notificações: " + e.getMessage());
        }
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

    @PatchMapping("/marcar-todas-lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(Authentication authentication) {
        String usuarioId = extrairIdUsuario(authentication);
        notificacaoService.marcarTodasComoLidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enviar-teste")
    public ResponseEntity<Void> enviarNotificacaoManual(@RequestBody NotificacaoTesteRequest request) {
        notificacaoService.criarEEnviarNotificacao(request.destinatarioId(), request.mensagem());
        return ResponseEntity.ok().build();
    }

    public record NotificacaoTesteRequest(String destinatarioId, String mensagem) {}

    private String extrairIdUsuario(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof BeneficiarioData b) {
            return b.getBeneficiario().getId();
        }

        if (principal instanceof ComercianteData c) {
            return c.getComerciante().getId();
        }

        if (principal instanceof AdministradorData a) {
            return a.getAdministrador().getId();
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de usuário desconhecido no sistema de notificação.");
    }
}
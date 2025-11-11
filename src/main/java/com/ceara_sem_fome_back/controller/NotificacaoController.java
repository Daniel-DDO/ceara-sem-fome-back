
package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.model.Pessoa;
import com.ceara_sem_fome_back.service.NotificacaoService;
import com.ceara_sem_fome_back.service.PessoaService; // Assumindo que você tem um serviço para pegar o usuário logado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private PessoaService pessoaService; // Serviço para buscar o usuário

    /**
     * Busca a lista de notificações para o usuário autenticado.
     */
    @GetMapping
    public ResponseEntity<List<Notificacao>> getNotificacoesDoUsuario(Authentication authentication) {
        Pessoa usuarioLogado = pessoaService.getUsuarioLogado(authentication); // Você precisa implementar este método
        List<Notificacao> notificacoes = notificacaoService.getNotificacoes(usuarioLogado);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * Busca a contagem de notificações não lidas para o usuário autenticado.
     */
    @GetMapping("/contagem-nao-lidas")
    public ResponseEntity<Long> getContagemNaoLidas(Authentication authentication) {
        Pessoa usuarioLogado = pessoaService.getUsuarioLogado(authentication); // Você precisa implementar este método
        long contagem = notificacaoService.getContagemNaoLidas(usuarioLogado);
        return ResponseEntity.ok(contagem);
    }

    /**
     * Marca uma notificação específica como lida.
     */
    @PostMapping("/{id}/marcar-lida")
    public ResponseEntity<?> marcarComoLida(@PathVariable Long id, Authentication authentication) {
        Pessoa usuarioLogado = pessoaService.getUsuarioLogado(authentication); // Você precisa implementar este método
        Notificacao notificacao = notificacaoService.marcarComoLida(id, usuarioLogado);
        if (notificacao != null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.model.Notificacao;
import com.ceara_sem_fome_back.model.Pessoa;
import com.ceara_sem_fome_back.service.NotificacaoService;
import com.ceara_sem_fome_back.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private PessoaService pessoaService; // Para identificar o usuário logado

    /**
     * Endpoint para buscar as notificações do usuário autenticado.
     *
     * @param authentication Objeto que contém os detalhes do usuário logado.
     * @return Uma lista de notificações para o usuário.
     */
    @GetMapping
    public ResponseEntity<List<Notificacao>> getNotificacoes(Authentication authentication) {
        // 1. Identifica o usuário logado
        Pessoa usuarioLogado = pessoaService.getUsuarioLogado(authentication);

        // 2. Busca as notificações usando o ID do usuário
        List<Notificacao> notificacoes = notificacaoService.buscarNotificacoesPorPessoa(usuarioLogado.getId());

        // 3. Retorna a lista
        return ResponseEntity.ok(notificacoes);
    }
}

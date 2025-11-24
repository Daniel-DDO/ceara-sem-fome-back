package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.model.Avaliacao;
import com.ceara_sem_fome_back.service.AvaliacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/avaliacoes")
@CrossOrigin(origins = {"https://ceara-raiz-srb9k.ondigitalocean.app/", "http://localhost:8080"})
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @GetMapping
    public ResponseEntity<PaginacaoDTO<Avaliacao>> listarAvaliacoes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "recentes") String sortBy
    ) {
        PaginacaoDTO<Avaliacao> pagina = avaliacaoService.listarAvaliacoes(
                page,
                size,
                sortBy,
                "desc"
        );

        return ResponseEntity.ok(pagina);
    }
}

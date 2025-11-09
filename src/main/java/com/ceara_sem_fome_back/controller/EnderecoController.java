package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.RegiaoResponse;
import com.ceara_sem_fome_back.service.EnderecoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/endereco")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping
    public ResponseEntity<RegiaoResponse> listarPorRegiao(
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String bairro) {
        RegiaoResponse response = enderecoService.listarEntidadesPorRegiao(municipio, bairro);
        return ResponseEntity.ok(response);
    }
}

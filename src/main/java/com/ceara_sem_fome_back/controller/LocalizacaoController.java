package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.localizacao.DistanciaResponse;
import com.ceara_sem_fome_back.service.BeneficiarioService;
import com.ceara_sem_fome_back.service.DistanciaService;
import com.ceara_sem_fome_back.service.EstabelecimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/localizacao")
public class LocalizacaoController {

    @Autowired
    private DistanciaService distanciaService;

    // Endpoint para calcular a distância entre um Beneficiário e um Estabelecimento.
    @GetMapping("/distancia")
    public DistanciaResponse calcularDistanciaEntreEntidades(
            @RequestParam String beneficiarioId,
            @RequestParam String estabelecimentoId) {

        double distanciaKm = distanciaService.calcularDistanciaEntreEntidades(beneficiarioId, estabelecimentoId);

        return new DistanciaResponse(distanciaKm, "km");
    }
}
package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.EstatisticasRegionaisDTO;
import com.ceara_sem_fome_back.service.EstatisticasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estatisticas")
public class EstatisticasController {

    @Autowired
    private EstatisticasService estatisticasService;

    @GetMapping("/municipio/{municipio}")
    public ResponseEntity<EstatisticasRegionaisDTO> getEstatisticasPorMunicipio(
            @PathVariable String municipio) {
        
        EstatisticasRegionaisDTO dto = estatisticasService.getEstatisticasPorMunicipio(municipio);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/bairro/{bairro}")
    public ResponseEntity<EstatisticasRegionaisDTO> getEstatisticasPorBairro(
            @PathVariable String bairro) {
        
        EstatisticasRegionaisDTO dto = estatisticasService.getEstatisticasPorBairro(bairro);
        return ResponseEntity.ok(dto);
    }
}
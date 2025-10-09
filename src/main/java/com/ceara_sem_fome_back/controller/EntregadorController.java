package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.LoginDTO;
import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.service.EntregadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/entregador"})
public class EntregadorController {

    @Autowired
    private EntregadorService entregadorService;

    @PostMapping("/login")
    public ResponseEntity<Entregador> logarEntregador(@RequestBody LoginDTO loginDTO) {
        Entregador entregador = entregadorService.logarEntregador(loginDTO.getLogin(), loginDTO.getSenha());

        if (entregador != null) {
            return ResponseEntity.ok(entregador);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }
}

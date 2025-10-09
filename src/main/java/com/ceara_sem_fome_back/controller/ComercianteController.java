package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.LoginDTO;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.service.ComercianteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/comerciante"})
public class ComercianteController {

    @Autowired
    private ComercianteService comercianteService;

    @PostMapping("/login")
    public ResponseEntity<Comerciante> logarComerciante(@RequestBody LoginDTO loginDTO) {
        Comerciante comerciante = comercianteService.logarComerciante(loginDTO.getEmail(), loginDTO.getSenha());

        if (comerciante != null) {
            return ResponseEntity.ok(comerciante);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }
}

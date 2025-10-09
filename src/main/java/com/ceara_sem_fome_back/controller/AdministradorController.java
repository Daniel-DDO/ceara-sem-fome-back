package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.LoginDTO;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/adm"})
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @PostMapping("/login")
    public ResponseEntity<Administrador> logarAdm(@RequestBody LoginDTO loginDTO) {
        Administrador administrador = administradorService.logarAdm(loginDTO.getEmail(), loginDTO.getSenha());

        if (administrador != null) {
            return ResponseEntity.ok(administrador);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }
}

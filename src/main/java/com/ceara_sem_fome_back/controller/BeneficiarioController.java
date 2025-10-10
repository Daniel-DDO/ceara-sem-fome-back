package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.service.BeneficiarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/beneficiario"})
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @PostMapping("/login")
    public ResponseEntity<Beneficiario> logarBeneficiario(@RequestBody LoginDTO loginDTO) {
        Beneficiario beneficiario = beneficiarioService.logarBeneficiario(loginDTO.getEmail(), loginDTO.getSenha());

        if (beneficiario != null) {
            return ResponseEntity.ok(beneficiario);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }
}

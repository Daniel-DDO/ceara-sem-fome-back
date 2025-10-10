package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.dto.AdministradorRequest;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.service.AdministradorService;
import jakarta.validation.Valid;
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


    @PostMapping
    public ResponseEntity<Object> cadastrarAdm(@RequestBody @Valid AdministradorRequest request) {
        try {
            Administrador administradorParaSalvar = new Administrador();
            administradorParaSalvar.setNome(request.getNome());
            administradorParaSalvar.setEmail(request.getEmail());
            administradorParaSalvar.setSenha(request.getSenha());

            Administrador novoAdministrador = administradorService.salvarAdm(administradorParaSalvar);

            return ResponseEntity.status(201).body(novoAdministrador);

        } catch (IllegalArgumentException e) {
            // Erros de regra de negócio (ex: email/CPF duplicado)
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);

        } catch (Exception e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar cadastrar administrador.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }
}

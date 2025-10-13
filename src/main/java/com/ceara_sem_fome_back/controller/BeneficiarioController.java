package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.dto.BeneficiarioRequest;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.service.BeneficiarioService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<Object> cadastrarBeneficiario(@RequestBody @Valid BeneficiarioRequest request) {
        try {
            Beneficiario novoBeneficiario = new Beneficiario(
                    request.getNome(),
                    request.getCpf(),
                    request.getEmail(),
                    request.getSenha(),
                    request.getDataNascimento(),
                    request.getTelefone(),
                    request.getGenero()
            );

            Beneficiario beneficiarioSalvo = beneficiarioService.salvarBeneficiario(novoBeneficiario);
            return ResponseEntity.status(201).body(beneficiarioSalvo);

        } catch (IllegalArgumentException e) {
            // Erros de regra de negócio (ex: CPF/Email duplicado)
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);

        } catch (Exception e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar cadastrar beneficiário.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }
}

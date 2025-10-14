package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.data.dto.PessoaRespostaDTO;
import com.ceara_sem_fome_back.dto.BeneficiarioRequest;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.BeneficiarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Scanner;

@RestController
@RequestMapping({"/beneficiario"})
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<PessoaRespostaDTO> logarBeneficiario(@RequestBody LoginDTO loginDTO) {
        Beneficiario beneficiario = beneficiarioService.logarBeneficiario(loginDTO.getEmail(), loginDTO.getSenha());

        if (beneficiario != null) {
            String token = jwtUtil.gerarToken(beneficiario.getEmail(), JWTUtil.TOKEN_EXPIRACAO);

            PessoaRespostaDTO responseDTO = new PessoaRespostaDTO(
                    beneficiario.getId(),
                    beneficiario.getNome(),
                    beneficiario.getEmail(),
                    token
            );

            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/cadastrar")
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

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Beneficiario>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PaginacaoDTO<Beneficiario> pagina = beneficiarioService.listarTodos(page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }

}

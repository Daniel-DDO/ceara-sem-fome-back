package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.data.dto.PessoaRespostaDTO;
import com.ceara_sem_fome_back.dto.ComercianteRequest;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.ComercianteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/comerciante"})
public class ComercianteController {

    @Autowired
    private ComercianteService comercianteService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<PessoaRespostaDTO> logarComerciante(@RequestBody LoginDTO loginDTO) {
        Comerciante comerciante = comercianteService.logarComerciante(loginDTO.getEmail(), loginDTO.getSenha());

        if (comerciante != null) {
            String token = jwtUtil.gerarToken(comerciante.getEmail(), JWTUtil.TOKEN_EXPIRACAO);

            PessoaRespostaDTO responseDTO = new PessoaRespostaDTO(
                    comerciante.getId(),
                    comerciante.getNome(),
                    comerciante.getEmail(),
                    token
            );

            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarComerciante(@RequestBody @Valid ComercianteRequest request) {
        try {
            Comerciante novoComerciante = new Comerciante(
                    request.getNome(),
                    request.getCpf(),
                    request.getEmail(),
                    request.getSenha(),
                    request.getDataNascimento(),
                    request.getTelefone(),
                    request.getGenero()
            );

            Comerciante comercianteSalvo = comercianteService.salvarComerciante(novoComerciante);

            return ResponseEntity.status(201).body(comercianteSalvo);

        } catch (IllegalArgumentException e) {
            // Erros de regra de negócio (ex: CPF/Email duplicado)
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);

        } catch (Exception e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar cadastrar comerciante.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Comerciante>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PaginacaoDTO<Comerciante> pagina = comercianteService.listarTodos(page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }
}

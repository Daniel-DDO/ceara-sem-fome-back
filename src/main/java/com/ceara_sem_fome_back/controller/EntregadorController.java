package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.data.dto.PessoaRespostaDTO;
import com.ceara_sem_fome_back.dto.EntregadorRequest;
import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.EntregadorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/entregador"})
public class EntregadorController {

    @Autowired
    private EntregadorService entregadorService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<PessoaRespostaDTO> logarEntregador(@RequestBody LoginDTO loginDTO) {
        Entregador entregador = entregadorService.logarEntregador(loginDTO.getEmail(), loginDTO.getSenha());

        if (entregador != null) {
            String token = jwtUtil.gerarToken(entregador.getEmail(), JWTUtil.TOKEN_EXPIRACAO);

            PessoaRespostaDTO responseDTO = new PessoaRespostaDTO(
                    entregador.getId(),
                    entregador.getNome(),
                    entregador.getEmail(),
                    token
            );

            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarEntregador(@RequestBody @Valid EntregadorRequest request) {
        try {
            Entregador novoEntregador = new Entregador(
                    request.getNome(),
                    request.getCpf(),
                    request.getEmail(),
                    request.getSenha(),
                    request.getDataNascimento(),
                    request.getTelefone(),
                    request.getGenero()
            );

            Entregador entregadorSalvo = entregadorService.salvarEntregador(novoEntregador);

            return ResponseEntity.status(201).body(entregadorSalvo);

        } catch (IllegalArgumentException e) {
            // Erros de regra de negócio (ex: CPF/Email duplicado)
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);

        } catch (Exception e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar cadastrar entregador.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Entregador>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PaginacaoDTO<Entregador> pagina = entregadorService.listarTodos(page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }
}

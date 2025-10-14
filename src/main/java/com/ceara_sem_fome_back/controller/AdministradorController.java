package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.data.dto.PessoaRespostaDTO;
import com.ceara_sem_fome_back.dto.AdministradorRequest;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.AdministradorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/adm")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<PessoaRespostaDTO> logarAdm(@RequestBody LoginDTO loginDTO) {
        Administrador administrador = administradorService.logarAdm(loginDTO.getEmail(), loginDTO.getSenha());

        if (administrador != null) {
            String token = jwtUtil.gerarToken(administrador.getEmail(), JWTUtil.TOKEN_EXPIRACAO);

            PessoaRespostaDTO responseDTO = new PessoaRespostaDTO(
                    administrador.getId(),
                    administrador.getNome(),
                    administrador.getEmail(),
                    token
            );
            return ResponseEntity.ok(responseDTO);
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

    @GetMapping("/all")
    public ResponseEntity<Page<Administrador>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Page<Administrador> pagina = administradorService.listarTodos(page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }
}

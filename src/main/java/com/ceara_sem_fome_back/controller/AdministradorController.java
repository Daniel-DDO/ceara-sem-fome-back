package com.ceara_sem_fome_back.controller;

//import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.data.dto.PessoaRespostaDTO;
import com.ceara_sem_fome_back.dto.AdministradorRequest;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.AdministradorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<PessoaRespostaDTO> logarAdm(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            if (loginDTO.getEmail() == null || loginDTO.getEmail().isBlank() ||
                    loginDTO.getSenha() == null || loginDTO.getSenha().isBlank()) {
                throw new IllegalArgumentException("Email e senha são obrigatórios.");
            }

            Administrador administrador = administradorService.logarAdm(
                    loginDTO.getEmail(),
                    loginDTO.getSenha()
            );

            if (administrador == null) {
                throw new RuntimeException("Email ou senha inválidos.");
            }

            String token = jwtUtil.gerarToken(administrador.getEmail(), JWTUtil.TOKEN_EXPIRACAO);

            return ResponseEntity.ok(new PessoaRespostaDTO(
                    administrador.getId(),
                    administrador.getNome(),
                    administrador.getEmail(),
                    token
            ));

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (RuntimeException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Erro interno do servidor.");
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarAdm(@RequestBody @Valid AdministradorRequest request) {
            Administrador administradorParaSalvar = new Administrador();
            administradorParaSalvar.setNome(request.getNome());
            administradorParaSalvar.setEmail(request.getEmail());
            administradorParaSalvar.setSenha(request.getSenha());

            Administrador novoAdministrador = administradorService.salvarAdm(administradorParaSalvar);

            return ResponseEntity.status(201).body(novoAdministrador);
    }

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Administrador>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PaginacaoDTO<Administrador> resposta = administradorService.listarTodos(page, size, sortBy, direction);
        return ResponseEntity.ok(resposta);
    }
}

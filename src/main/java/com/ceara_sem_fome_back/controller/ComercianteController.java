package com.ceara_sem_fome_back.controller;

//import com.ceara_sem_fome_back.data.dto.ErrorDTO;
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
    public ResponseEntity<PessoaRespostaDTO> logarComerciante(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            if (loginDTO.getEmail() == null || loginDTO.getEmail().isBlank() ||
                    loginDTO.getSenha() == null || loginDTO.getSenha().isBlank()) {
                throw new IllegalArgumentException("Email e senha são obrigatórios.");
            }

            Comerciante comerciante = comercianteService.logarComerciante(
                    loginDTO.getEmail(),
                    loginDTO.getSenha()
            );

            if (comerciante == null) {
                throw new RuntimeException("Email ou senha inválidos.");
            }

            String token = jwtUtil.gerarToken(comerciante.getEmail(), JWTUtil.TOKEN_EXPIRACAO);

            return ResponseEntity.ok(new PessoaRespostaDTO(
                    comerciante.getId(),
                    comerciante.getNome(),
                    comerciante.getEmail(),
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

    @PostMapping("/iniciar-cadastro")
    public ResponseEntity<Object> iniciarCadastroComerciante(@RequestBody @Valid ComercianteRequest request) {
        try {
            comercianteService.iniciarCadastro(request);
            return ResponseEntity.status(202).body("Verifique seu e-mail para continuar o cadastro.");
        } catch (IllegalArgumentException e) {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);
        } catch (Exception e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar iniciar o cadastro.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarComerciante(@RequestBody @Valid ComercianteRequest request) {
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

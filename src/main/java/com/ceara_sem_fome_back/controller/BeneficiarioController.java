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
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/beneficiario")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<PessoaRespostaDTO> logarBeneficiario(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            if (loginDTO.getEmail() == null || loginDTO.getEmail().isBlank() ||
                    loginDTO.getSenha() == null || loginDTO.getSenha().isBlank()) {
                throw new IllegalArgumentException("Email e senha são obrigatórios.");
            }

            Beneficiario beneficiario = beneficiarioService.logarBeneficiario(
                    loginDTO.getEmail(),
                    loginDTO.getSenha()
            );

            String token = jwtUtil.gerarToken(beneficiario.getEmail(), JWTUtil.TOKEN_EXPIRACAO);

            return ResponseEntity.ok(new PessoaRespostaDTO(
                    beneficiario.getId(),
                    beneficiario.getNome(),
                    beneficiario.getEmail(),
                    token
            ));

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (RuntimeException e) {
            throw new RuntimeException("Email ou senha inválidos.");

        } catch (Exception e) {
            throw new RuntimeException("Erro interno do servidor.");
        }
    }

    /**
     * Rota para iniciar o processo de cadastro.
     * Recebe os dados do usuário, verifica se já existem e envia o e-mail de confirmação.
     */
    @PostMapping("/iniciar-cadastro") // Endpoint renomeado para maior clareza
    public ResponseEntity<Object> iniciarCadastroBeneficiario(@RequestBody @Valid BeneficiarioRequest request) {
        try {
            // Este método agora só envia o e-mail
            beneficiarioService.iniciarCadastro(request);
            return ResponseEntity.status(202).body("Verifique seu e-mail para continuar o cadastro.");
        } catch (IllegalArgumentException e) {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);
        } catch (Exception e) {
            // Logar o erro real aqui é uma boa prática
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar iniciar o cadastro.", 500);
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
package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.data.dto.PessoaRespostaDTO;
import com.ceara_sem_fome_back.dto.AdministradorRequest;
import com.ceara_sem_fome_back.dto.PessoaUpdateDto;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.AdministradorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/adm")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<PessoaRespostaDTO> logarAdm(@Valid @RequestBody LoginDTO loginDTO) {
        //metodo de login
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
        //metodo de cadastro
            Administrador administradorParaSalvar = new Administrador();
            administradorParaSalvar.setNome(request.getNome());
            administradorParaSalvar.setEmail(request.getEmail());
            administradorParaSalvar.setSenha(request.getSenha());

            Administrador novoAdministrador = administradorService.salvarAdm(administradorParaSalvar);

            return ResponseEntity.status(201).body(novoAdministrador);
    }

    @PostMapping("/iniciar-cadastro")
    public ResponseEntity<Object> iniciarCadastroAdministrador(@RequestBody @Valid AdministradorRequest request) {
        //metodo de iniciar-cadastro
        try {
            administradorService.iniciarCadastro(request);
            return ResponseEntity.status(202).body("Verifique seu e-mail para continuar o cadastro.");
        } catch (IllegalArgumentException e) {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);
        } catch (Exception e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar iniciar o cadastro.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Administrador>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        //metodo de listagem
        PaginacaoDTO<Administrador> resposta = administradorService.listarTodos(page, size, sortBy, direction);
        return ResponseEntity.ok(resposta);
    }

    /**
     * Endpoint para o usuário autenticado (Administrador) atualizar seus próprios dados.
     * O usuário é identificado pelo token JWT.
     */
    @PutMapping("/meu-perfil")
    public ResponseEntity<Administrador> atualizarPerfil(
            @Valid @RequestBody PessoaUpdateDto dto,
            Principal principal) { //Pega o usuário autenticado via token

        //1. Pega o e-mail do usuário logado (armazenado no token)
        String userEmail = principal.getName(); 
        
        //2. Chama o novo serviço de atualização
        Administrador adminAtualizado = administradorService.atualizarAdministrador(userEmail, dto);
        
        //3. A senha não retorna no JSON.
        adminAtualizado.setSenha(null); 

        //4. Retorna o objeto atualizado
        return ResponseEntity.ok(adminAtualizado);
    }

    //ENDPOINT DE REATIVAÇÃO

    /**
     * Endpoint para reativar uma conta que foi desativada (soft delete).
     * @param id O ID do usuário
     * @return Resposta 200 OK com mensagem de sucesso.
     */
    @PutMapping("/conta/{id}/reativar")
    public ResponseEntity<Object> reativarConta(@PathVariable String id) {
        try {
            administradorService.reativarConta(id);
            return ResponseEntity.ok().body(Map.of("message", "Conta reativada com sucesso."));
        } catch (RecursoNaoEncontradoException e) {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
        } catch (Exception e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar reativar a conta.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }
}
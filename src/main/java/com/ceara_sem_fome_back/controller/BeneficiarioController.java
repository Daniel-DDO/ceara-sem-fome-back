package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.data.dto.LoginDTO;
import com.ceara_sem_fome_back.data.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.data.dto.PessoaRespostaDTO;
import com.ceara_sem_fome_back.dto.BeneficiarioRequest;
import com.ceara_sem_fome_back.dto.PessoaUpdateDto;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.BeneficiarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/beneficiario")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<PessoaRespostaDTO> logarBeneficiario(@Valid @RequestBody LoginDTO loginDTO) {
        //metodo de login
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
    @PostMapping("/iniciar-cadastro") //Endpoint renomeado para maior clareza
    public ResponseEntity<Object> iniciarCadastroBeneficiario(@RequestBody @Valid BeneficiarioRequest request) {
        //metodo de cadastro
        try {
            //Este metodo agora só envia o e-mail
            beneficiarioService.iniciarCadastro(request);
            return ResponseEntity.status(202).body("Verifique seu e-mail para continuar o cadastro.");
        } catch (IllegalArgumentException e) {
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);
        } catch (Exception e) {
            //Loga o erro real
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar iniciar o cadastro.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Beneficiario>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "") String nomeFiltro
    ) {
        //metodo de listagem
        PaginacaoDTO<Beneficiario> pagina = beneficiarioService.listarComFiltro(nomeFiltro, page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }

    //NOVO ENDPOINT DE ATUALIZAÇÃO DE PERFIL
    /**
     * Endpoint para o usuário autenticado (Beneficiário) atualizar seus próprios dados.
     * O usuário é identificado pelo token JWT.
     */
    @PutMapping("/meu-perfil")
    public ResponseEntity<Beneficiario> atualizarPerfil(
            @Valid @RequestBody PessoaUpdateDto dto,
            Principal principal) { //Pega o usuário autenticado via token

        //1. O 'Principal' injetado pelo Spring Security contém o usuário.
        //No caso (JWT), principal.getName() retorna o E-MAIL do token.
        String userEmail = principal.getName(); 
        
        //2. Chama o serviço que você criou
        Beneficiario beneficiarioAtualizado = beneficiarioService.atualizarBeneficiario(userEmail, dto);
        
        //3. A senha não retorna no JSON.
        beneficiarioAtualizado.setSenha(null); 

        //4. Retorna o objeto atualizado com status 200 OK
        return ResponseEntity.ok(beneficiarioAtualizado);
    }

    @GetMapping("/filtrar/cpf")
    public ResponseEntity<Beneficiario> filtrarPorCpf(
            @RequestParam(name = "valor") String cpf) {

        Beneficiario beneficiario = beneficiarioService.filtrarPorCpf(cpf);

        // 💡 A senha não retorna no JSON
        beneficiario.setSenha(null);

        return ResponseEntity.ok(beneficiario);
    }
}
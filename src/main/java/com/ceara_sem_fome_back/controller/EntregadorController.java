package com.ceara_sem_fome_back.controller;

//import com.ceara_sem_fome_back.dto.ErrorDTO;
import com.ceara_sem_fome_back.dto.ErrorDTO;
import com.ceara_sem_fome_back.dto.LoginDTO;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.PessoaRespostaDTO;
import com.ceara_sem_fome_back.dto.AlterarStatusRequest;
import com.ceara_sem_fome_back.dto.EntregadorRequest;
import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.model.StatusPessoa;
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
    public ResponseEntity<PessoaRespostaDTO> logarEntregador(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            if (loginDTO.getEmail() == null || loginDTO.getEmail().isBlank() ||
                    loginDTO.getSenha() == null || loginDTO.getSenha().isBlank()) {
                throw new IllegalArgumentException("Email e senha são obrigatórios.");
            }

            Entregador entregador = entregadorService.logarEntregador(
                    loginDTO.getEmail(),
                    loginDTO.getSenha()
            );

            if (entregador == null) {
                throw new RuntimeException("Email ou senha inválidos.");
            }

            String token = jwtUtil.gerarToken(entregador.getEmail(), JWTUtil.TOKEN_EXPIRACAO);

            return ResponseEntity.ok(new PessoaRespostaDTO(
                    entregador.getId(),
                    entregador.getNome(),
                    entregador.getEmail(),
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
    public ResponseEntity<Object> iniciarCadastroEntregador(@RequestBody @Valid EntregadorRequest request) {
        try {
            entregadorService.iniciarCadastro(request);
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
    public ResponseEntity<Object> cadastrarEntregador(@RequestBody @Valid EntregadorRequest request) {
            Entregador novoEntregador = new Entregador(
                    request.getNome(),
                    request.getCpf(),
                    request.getEmail(),
                    request.getSenha(),
                    request.getDataNascimento(),
                    request.getTelefone(),
                    request.getGenero(),
                    request.getLgpdAccepted()
            );

            Entregador entregadorSalvo = entregadorService.salvarEntregador(novoEntregador);

            return ResponseEntity.status(201).body(entregadorSalvo);
    }

    @PatchMapping("/{id}/alterar-status")
    public ResponseEntity<Entregador> alterarStatusEntregador(@PathVariable AlterarStatusRequest request ) {
        Entregador entregadorAtualizado = entregadorService.alterarStatusEntregador(request);
        return ResponseEntity.ok(entregadorAtualizado);
    }

    @PatchMapping("/{id}/bloquear-conta")
    public ResponseEntity<Entregador> bloquearContaEntregador(@PathVariable AlterarStatusRequest request) {
        request.setNovoStatusPessoa(StatusPessoa.BLOQUEADO);
        Entregador entregadorBloqueado = entregadorService.alterarStatusEntregador(request);
        return ResponseEntity.ok(entregadorBloqueado);
    }

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Entregador>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "") String nomeFiltro
    ) {
        PaginacaoDTO<Entregador> pagina = entregadorService.listarComFiltro(nomeFiltro, page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/filtrar/cpf")
    public ResponseEntity<Entregador> filtrarPorCpf(
            @RequestParam(name = "valor") String cpf) {

        Entregador entregador = entregadorService.filtrarPorCpf(cpf);

        //A senha não retorna no JSON
        entregador.setSenha(null);

        return ResponseEntity.ok(entregador);
    }
}

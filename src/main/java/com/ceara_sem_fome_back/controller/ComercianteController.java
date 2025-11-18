package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.dto.ContaDTO;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.StatusPessoa;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping({"/comerciante"})
public class ComercianteController {

    @Autowired
    private ComercianteService comercianteService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private EstabelecimentoService estabelecimentoService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private CompraService compraService;

    @Autowired
    private ProdutoEstabelecimentoService produtoEstabelecimentoService;

    @PostMapping("/login")
    public ResponseEntity<PessoaRespostaDTO> logarComerciante(@Valid @RequestBody LoginDTO loginDTO) {
        //metodo de login
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
        //metodo de iniciar-cadastro
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
        //metodo de cadastro existente
            Comerciante novoComerciante = new Comerciante(
                    request.getNome(),
                    request.getCpf(),
                    request.getEmail(),
                    request.getSenha(),
                    request.getDataNascimento(),
                    request.getTelefone(),
                    request.getGenero(),
                    request.getLgpdAccepted()
            );

            Comerciante comercianteSalvo = comercianteService.salvarComerciante(novoComerciante);

            return ResponseEntity.status(201).body(comercianteSalvo);
    }


    // Endpoint para alterar o status ativo/inativo do comerciante
    @PatchMapping("/{id}/alterar-status")
    public ResponseEntity<Comerciante> alterarStatusComerciante(@PathVariable AlterarStatusRequest request) {
        Comerciante comercianteAtualizado = comercianteService.alterarStatusComerciante(request);
        return ResponseEntity.ok(comercianteAtualizado);
    }

    // Endpoint para bloquear conta do comerciante
    @PatchMapping("/{id}/bloquear-conta")
    public ResponseEntity<Comerciante> bloquearContaComerciante(@PathVariable AlterarStatusRequest request) {
        request.setNovoStatusPessoa(StatusPessoa.BLOQUEADO);
        Comerciante comercianteBloqueado = comercianteService.alterarStatusComerciante(request);
        return ResponseEntity.ok(comercianteBloqueado);
    }

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Comerciante>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String nomeFiltro
    ) {
        //metodo de listagem
        PaginacaoDTO<Comerciante> pagina = comercianteService.listarComFiltro(nomeFiltro, page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }

    /**
     * Endpoint para o usuário autenticado (Comerciante) atualizar seus próprios dados.
     * O usuário é identificado pelo token JWT.
     */
    @PutMapping("/meu-perfil")
    public ResponseEntity<Comerciante> atualizarPerfil(
            @Valid @RequestBody PessoaUpdateDto dto,
            Principal principal) { //Pega o usuário autenticado via token

        //1. Pega o e-mail do usuário logado (armazenado no token)
        String userEmail = principal.getName(); 
        
        //2. Chama o novo serviço de atualização
        Comerciante comercianteAtualizado = comercianteService.atualizarComerciante(userEmail, dto);
        
        //3. A senha não retorna no JSON.
        comercianteAtualizado.setSenha(null); 

        // 4. Retorna o objeto atualizado
        return ResponseEntity.ok(comercianteAtualizado);
    }

    @GetMapping("/filtrar/cpf")
    public ResponseEntity<Comerciante> filtrarPorCpf(
            @RequestParam(name = "valor") String cpf) {

        Comerciante comerciante = comercianteService.filtrarPorCpf(cpf);
        // A senha não retorna no JSON
        comerciante.setSenha(null);

        return ResponseEntity.ok(comerciante);
    }

    @GetMapping("/meus-estabelecimentos")
    public ResponseEntity<List<EstabelecimentoRespostaDTO>> listarMeusEstabelecimentos(
            @AuthenticationPrincipal ComercianteData comercianteData) {

        String comercianteId = comercianteData.getComerciante().getId();
        List<EstabelecimentoRespostaDTO> estabelecimentos =
                estabelecimentoService.listarPorComerciante(comercianteId);
        return ResponseEntity.ok(estabelecimentos);
    }

    @GetMapping("/meu-historico-vendas")
    public ResponseEntity<List<HistoricoVendasDTO>> listarHistoricoVendas(
            @AuthenticationPrincipal ComercianteData comercianteData) {

        String comercianteId = comercianteData.getComerciante().getId();
        List<HistoricoVendasDTO> historico = compraService.getHistoricoVendasPorComerciante(comercianteId);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/meus-produtos")
    public ResponseEntity<List<ProdutoDTO>> listarProdutos(@AuthenticationPrincipal ComercianteData comercianteData) {
        String comercianteId = comercianteData.getComerciante().getId();
        List<ProdutoDTO> produtos = produtoService.listarPorComerciante(comercianteId);
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/meu-extrato")
    public ResponseEntity<ContaDTO> consultarExtrato(
            @AuthenticationPrincipal ComercianteData comercianteData) {

        String comercianteId = comercianteData.getComerciante().getId();
        ContaDTO contaDTO = comercianteService.consultarExtrato(comercianteId);
        return ResponseEntity.ok(contaDTO);
    }

    @PostMapping("/adicionar-prod-estab")
    public ResponseEntity<String> adicionarProduto(
            @RequestParam String produtoId,
            @RequestParam String estabelecimentoId
    ) {
        produtoEstabelecimentoService.adicionarProdutoEmEstabelecimento(produtoId, estabelecimentoId);
        return ResponseEntity.ok("Produto vinculado ao estabelecimento com sucesso.");
    }

    @DeleteMapping("/remover-prod-estab")
    public ResponseEntity<String> removerProduto(
            @RequestParam String produtoId,
            @RequestParam String estabelecimentoId
    ) {
        produtoEstabelecimentoService.removerProdutoDeEstabelecimento(produtoId, estabelecimentoId);
        return ResponseEntity.ok("Produto desvinculado do estabelecimento com sucesso.");
    }

    @PutMapping("/atualizar-estoque-prod-estab")
    public ResponseEntity<String> atualizarEstoque(
            @RequestParam String produtoId,
            @RequestParam String estabelecimentoId,
            @RequestParam int quantidade
    ) {
        produtoEstabelecimentoService.atualizarEstoque(produtoId, estabelecimentoId, quantidade);
        return ResponseEntity.ok("Estoque atualizado com sucesso.");
    }

    @DeleteMapping("/deletar/{idProdEstab}")
    public ResponseEntity<String> deletarProdEstab(
            @PathVariable String idProdEstab
    ) {
        produtoEstabelecimentoService.deletarProdEstab(idProdEstab);
        return ResponseEntity.ok("Produto desvinculado do estabelecimento definitivamente.");
    }
}
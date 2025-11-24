package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.exception.NegocioException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Carrinho;
import com.ceara_sem_fome_back.model.Compra;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.AvaliacaoService;
import com.ceara_sem_fome_back.service.BeneficiarioService;
import com.ceara_sem_fome_back.service.EnderecoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/beneficiario")
public class BeneficiarioController {

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private EnderecoService enderecoService;

    @Autowired
    private AvaliacaoService avaliacaoService;

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

    // Endpoint para desativar um beneficiário
    @PatchMapping("/{id}/alterar-status")
    public ResponseEntity<Beneficiario> desativarBeneficiario(@PathVariable AlterarStatusRequest request) {
        Beneficiario beneficiarioDesativado = beneficiarioService.alterarStatusBeneficiario(request);
        return ResponseEntity.ok(beneficiarioDesativado);
    }

    // Endpoint para reativar um beneficiário
    @PatchMapping("/{id}/reativar-beneficiario")
    public ResponseEntity<Beneficiario> reativarBeneficiario(@PathVariable AlterarStatusRequest request) {
        Beneficiario beneficiarioReativado = beneficiarioService.alterarStatusBeneficiario(request);
        return ResponseEntity.ok(beneficiarioReativado);
    }

    //Endpoint para bloquear um beneficiário
    @PatchMapping("/{id}/bloquear-beneficiario")
    public ResponseEntity<Beneficiario> bloquearBeneficiario(@PathVariable AlterarStatusRequest request) {
        Beneficiario beneficiarioBloqueado = beneficiarioService.alterarStatusBeneficiario(request);
        return ResponseEntity.ok(beneficiarioBloqueado);
    }

    //Endpoint para um beneficiário adicionar um endereço
//    @PostMapping("/{beneficiarioId}/endereco") //verificar se beneficiarioId existe
//    public ResponseEntity<Beneficiario> adicionarEndereco(
//            @PathVariable String beneficiarioId,
//            @RequestBody Endereco enderecoRequest) {
//        Beneficiario beneficiario = beneficiarioService.adicionarEndereco(beneficiarioId, enderecoRequest);
//        return ResponseEntity.ok(beneficiario);
//    }

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

    @GetMapping("/bairro/{bairro}")
    public ResponseEntity<List<Beneficiario>> listarPorBairro(@PathVariable String bairro) {
        return ResponseEntity.ok(beneficiarioService.buscarPorBairro(bairro));
    }

    @GetMapping("/municipio/{municipio}")
    public ResponseEntity<List<Beneficiario>> listarPorMunicipio(@PathVariable String municipio) {
        return ResponseEntity.ok(beneficiarioService.buscarPorMunicipio(municipio));
    }

    @GetMapping("/filtrar/cpf")
    public ResponseEntity<Beneficiario> filtrarPorCpf(
            @RequestParam(name = "valor") String cpf) {

        Beneficiario beneficiario = beneficiarioService.filtrarPorCpf(cpf);

        //A senha não retorna no JSON
        beneficiario.setSenha(null);

        return ResponseEntity.ok(beneficiario);
    }

    @PostMapping("/cadastrar-endereco")
    public ResponseEntity<Beneficiario> cadastrarEndereco(@Valid @RequestBody EnderecoCadRequest enderecoCadRequest) {

        BeneficiarioData beneficiarioData = (BeneficiarioData) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String beneficiarioId = beneficiarioData.getId();
        Beneficiario beneficiarioAtualizado = enderecoService.cadastrarEnderecoBenef(beneficiarioId, enderecoCadRequest);

        return ResponseEntity.ok(beneficiarioAtualizado);
    }

    @GetMapping("/conta/balanco")
    public ResponseEntity<BigDecimal> verBalanco(Principal principal) {
        String userEmail = principal.getName();
        BigDecimal saldo = beneficiarioService.verBalanco(userEmail);
        return ResponseEntity.ok(saldo);
    }

    /*
    @PostMapping("/compra")
    public ResponseEntity<Compra> realizarCompra(Principal principal) {
        String userEmail = principal.getName();
        Compra novaCompra = beneficiarioService.realizarCompra(userEmail);
        return ResponseEntity.status(201).body(novaCompra);
    }

     */

    @GetMapping("/compras/historico")
    public ResponseEntity<List<Compra>> verHistoricoCompras(Principal principal) {
        String userEmail = principal.getName();
        List<Compra> historico = beneficiarioService.verHistoricoCompras(userEmail);
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/carrinho")
    public ResponseEntity<Carrinho> verCarrinho(Principal principal) {
        String userEmail = principal.getName();
        Carrinho carrinho = beneficiarioService.verCarrinho(userEmail);
        return ResponseEntity.ok(carrinho);
    }

    @PostMapping("/carrinho/item")
    public ResponseEntity<Carrinho> manipularCarrinho(
            @Valid @RequestBody ManipularCarrinhoDTO dto,
            Principal principal) {

        String userEmail = principal.getName();
        Carrinho carrinhoAtualizado = beneficiarioService.manipularCarrinho(
                userEmail,
                dto.getProdutoId(),
                dto.getQuantidade());

        return ResponseEntity.ok(carrinhoAtualizado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BeneficiarioRespostaDTO> retornaBeneficiario(
            @PathVariable String id) {
        Beneficiario beneficiario = beneficiarioService.buscarPorId(id);

        if (beneficiario == null) {
            return ResponseEntity.notFound().build();
        }

        EnderecoRespostaDTO end = null;
        if (beneficiario.getEndereco() != null) {
            end = new EnderecoRespostaDTO();
            end.setId(beneficiario.getEndereco().getId());
            end.setCep(beneficiario.getEndereco().getCep());
            end.setLogradouro(beneficiario.getEndereco().getLogradouro());
            end.setNumero(beneficiario.getEndereco().getNumero());
            end.setBairro(beneficiario.getEndereco().getBairro());
            end.setMunicipio(beneficiario.getEndereco().getMunicipio());
        }

        BeneficiarioRespostaDTO beneficiarioRespostaDTO = new BeneficiarioRespostaDTO();
        beneficiarioRespostaDTO.setId(beneficiario.getId());
        beneficiarioRespostaDTO.setNome(beneficiario.getNome());
        beneficiarioRespostaDTO.setCpf(beneficiario.getCpf());
        beneficiarioRespostaDTO.setEmail(beneficiario.getEmail());
        beneficiarioRespostaDTO.setDataNascimento(beneficiario.getDataNascimento());
        beneficiarioRespostaDTO.setTelefone(beneficiario.getTelefone());
        beneficiarioRespostaDTO.setGenero(beneficiario.getGenero());
        beneficiarioRespostaDTO.setLgpdAccepted(beneficiario.getLgpdAccepted());
        beneficiarioRespostaDTO.setNumeroCadastroSocial(beneficiario.getNumeroCadastroSocial());
        beneficiarioRespostaDTO.setConta(beneficiario.getConta());
        beneficiarioRespostaDTO.setEndereco(end);

        return ResponseEntity.ok(beneficiarioRespostaDTO);
    }

    @PostMapping("/compra/avaliar")
    public ResponseEntity<Void> avaliarCompra(
            @Valid @RequestBody AvaliacaoRequestDTO dto,
            @AuthenticationPrincipal BeneficiarioData beneficiarioData) {

        if (beneficiarioData == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String beneficiarioId = beneficiarioData.getId();
            avaliacaoService.registrarAvaliacao(dto.getCompraId(), beneficiarioId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NegocioException e) {
            return ResponseEntity.status(e.getStatus()).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<BeneficiarioRespostaDTO> obterBeneficiarioLogado(
            @AuthenticationPrincipal BeneficiarioData beneficiarioData) {

        String beneficiarioId = beneficiarioData.getBeneficiario().getId();
        BeneficiarioRespostaDTO dto = beneficiarioService.buscarPorIdDto(beneficiarioId);

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(
            @RequestBody @Valid AlterarSenhaRequest request,
            Authentication authentication
    ) {
        BeneficiarioData beneficiarioData = (BeneficiarioData) authentication.getPrincipal();

        Beneficiario beneficiario = beneficiarioData.getBeneficiario();
        beneficiarioService.alterarSenha(beneficiario.getId(), request);

        return ResponseEntity.noContent().build();
    }

}
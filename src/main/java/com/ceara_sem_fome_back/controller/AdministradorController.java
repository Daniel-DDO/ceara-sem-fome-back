package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private ComunicadoService comunicadoService;

    @Autowired
    private ComercianteService comercianteService;

    @Autowired
    private BeneficiarioService beneficiarioService;

    @Autowired
    private EntregadorService entregadorService;

    @Autowired
    private CompraService compraService;

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

    //DESATIVAR qualquer conta
    @PatchMapping("/desativar/{tipo}/{id}")
    public ResponseEntity<Pessoa> desativar(@PathVariable String tipo, @PathVariable String id) {
        return processarAlteracaoStatus(tipo, id, StatusPessoa.INATIVO);
    }

    //BLOQUEAR qualquer conta
    @PatchMapping("/bloquear/{tipo}/{id}")
    public ResponseEntity<Pessoa> bloquear(@PathVariable String tipo, @PathVariable String id) {
        return processarAlteracaoStatus(tipo, id, StatusPessoa.BLOQUEADO);
    }

    //ATIVAR qualquer conta
    @PatchMapping("/ativar/{tipo}/{id}")
    public ResponseEntity<Pessoa> ativar(@PathVariable String tipo, @PathVariable String id) {
        return processarAlteracaoStatus(tipo, id, StatusPessoa.ATIVO);
    }

    //tornar qualquer conta PENDENTE
    @PatchMapping("/pendente/{tipo}/{id}")
    public ResponseEntity<Pessoa> pendente(@PathVariable String tipo, @PathVariable String id) {
        return processarAlteracaoStatus(tipo, id, StatusPessoa.PENDENTE);
    }

    //RECUSAR qualquer conta
    @PatchMapping("/recusar/{tipo}/{id}")
    public ResponseEntity<Pessoa> recusar(@PathVariable String tipo, @PathVariable String id) {
        return processarAlteracaoStatus(tipo, id, StatusPessoa.RECUSADO);
    }

    private ResponseEntity<Pessoa> processarAlteracaoStatus(String tipoStr, String id, StatusPessoa status) {
        try {

            TipoPessoa tipoPessoa = TipoPessoa.valueOf(tipoStr.toUpperCase());
            Pessoa pessoaAtualizada = administradorService.alterarStatus(id, tipoPessoa, status);

            return ResponseEntity.ok(pessoaAtualizada);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de pessoa inválido: " + tipoStr);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
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
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "") String nomeFiltro
    ) {
        //metodo de listagem
        PaginacaoDTO<Administrador> resposta = administradorService.listarComFiltro(nomeFiltro, page, size, sortBy, direction);
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

        String userEmail = principal.getName();
        Administrador adminAtualizado = administradorService.atualizarAdministrador(userEmail, dto);
        //A senha não retorna no JSON.
        adminAtualizado.setSenha(null);
        return ResponseEntity.ok(adminAtualizado);
    }

    @GetMapping("/filtrar/cpf")
    public ResponseEntity<Administrador> filtrarPorCpf(
            @RequestParam(name = "valor") String cpf) {

        Administrador administrador = administradorService.filtrarPorCpf(cpf);
        administrador.setSenha(null);

        return ResponseEntity.ok(administrador);
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

    @PatchMapping("/aprovar-produto/{id}")
    public ResponseEntity<ProdutoDTO> aprovarProduto(
            @PathVariable String id,
            @AuthenticationPrincipal AdministradorData administradorData) {

        if (administradorData == null) {
            throw new AccessDeniedException("Acesso negado. Apenas administradores podem aprovar produtos.");
        }

        Administrador administradorLogado = administradorData.getAdministrador();
        Produto produtoAprovado = administradorService.aprovarProduto(id, administradorLogado);

        return ResponseEntity.ok(paraDTO(produtoAprovado));
    }

    @PatchMapping("/recusar-produto/{id}")
    public ResponseEntity<ProdutoDTO> recusarProduto(
            @PathVariable String id,
            @AuthenticationPrincipal AdministradorData administradorData) {

        if (administradorData == null) {
            throw new AccessDeniedException("Acesso negado. Apenas administradores podem recusar produtos.");
        }

        Administrador administradorLogado = administradorData.getAdministrador();
        Produto produtoRecusado = administradorService.recusarProduto(id, administradorLogado);

        return ResponseEntity.ok(paraDTO(produtoRecusado));
    }

    private ProdutoDTO paraDTO(Produto produto) {
        if (produto == null) return null;

        return new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getLote(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getStatus(),
                produto.getCategoria(),
                produto.getUnidade(),
                produto.getImagem(),
                produto.getTipoImagem(),
                produto.getDataCadastro(),
                produto.getAvaliadoPorId(),
                produto.getDataAvaliacao(),
                produto.getComerciante().getId()
        );
    }

    // endpoint para listar todos os estabelecimentos
    @GetMapping("/estabelecimentos")
    public ResponseEntity<List<EstabelecimentoRespostaDTO>> listarEstabelecimentos(
            @AuthenticationPrincipal AdministradorData administradorData) {

        Administrador adminLogado = administradorData.getAdministrador();

        List<Estabelecimento> estabelecimentos =
                administradorService.listarTodosEstabelecimentos(adminLogado);

        List<EstabelecimentoRespostaDTO> resposta = estabelecimentos.stream()
                .map(est -> new EstabelecimentoRespostaDTO(
                est.getId(),
                est.getNome(),
                est.getCnpj(),
                est.getTelefone(),
                est.getImagem(),
                est.getTipoImagem(),
                est.getMediaAvaliacoes(),
                est.getEndereco() != null ? est.getEndereco().getId() : null,
                est.getEndereco() != null ? est.getEndereco().getCep() : null,
                est.getEndereco() != null ? est.getEndereco().getLogradouro() : null,
                est.getEndereco() != null ? est.getEndereco().getNumero() : null,
                est.getEndereco() != null ? est.getEndereco().getBairro() : null,
                est.getEndereco() != null ? est.getEndereco().getMunicipio() : null,
                        est.getEndereco() != null ? est.getEndereco().getLatitude() : null,
                        est.getEndereco() != null ? est.getEndereco().getLongitude() : null,
                est.getComerciante() != null ? est.getComerciante().getId() : null,
                est.getComerciante() != null ? est.getComerciante().getNome() : null
        )
)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    // enpoints para o administrador obter informações do beneficiário e do comerciante
    @GetMapping("/comerciantes")
    public ResponseEntity<List<ComercianteRespostaDTO>> listarComerciantes() {
        List<ComercianteRespostaDTO> comerciantes = administradorService.listarComerciantes();
        return ResponseEntity.ok(comerciantes);
    }

    // Endpoint para listar todos os beneficiários
    @GetMapping("/beneficiarios")
    public ResponseEntity<List<BeneficiarioRespostaDTO>> listarBeneficiarios() {
        List<BeneficiarioRespostaDTO> beneficiarios = administradorService.listarBeneficiarios();
        return ResponseEntity.ok(beneficiarios);
    }

    // Endpoint único para pegar todos os dados juntos
    @GetMapping("/dados-completos")
    public ResponseEntity<DadosCompletosDTO> obterDadosCompletos() {
        DadosCompletosDTO dados = administradorService.obterDadosCompletos();
        return ResponseEntity.ok(dados);
    }

    @PatchMapping("/aprovar-comerciante/{id}")
    public ResponseEntity<Comerciante> aprovarComerciante(
            @PathVariable String id,
            @AuthenticationPrincipal AdministradorData administradorData) {

        if (administradorData == null) {
            throw new AccessDeniedException("Acesso negado. Apenas administradores podem aprovar comerciantes.");
        }

        Comerciante comercianteAprovado = administradorService.aprovarComerciante(id);
        return ResponseEntity.ok(comercianteAprovado);
    }

    @PatchMapping("/recusar-comerciante/{id}")
    public ResponseEntity<Comerciante> recusarComerciante(
            @PathVariable String id,
            @AuthenticationPrincipal AdministradorData administradorData) {

        if (administradorData == null) {
            throw new AccessDeniedException("Acesso negado. Apenas administradores podem recusar comerciantes.");
        }

        Comerciante comercianteRecusado = administradorService.recusarComerciante(id);
        return ResponseEntity.ok(comercianteRecusado);
    }

    @PatchMapping("/bloquear-comerciante/{id}")
    public ResponseEntity<Comerciante> bloquearComerciante(
            @PathVariable String id,
            @AuthenticationPrincipal AdministradorData administradorData) {

        if (administradorData == null) {
            throw new AccessDeniedException("Acesso negado. Apenas administradores podem bloquear comerciantes.");
        }

        Comerciante comercianteBloqueado = administradorService.bloquearComerciante(id);
        return ResponseEntity.ok(comercianteBloqueado);
    }

    @PatchMapping("/inativar-comerciante/{id}")
    public ResponseEntity<Comerciante> inativarComerciante(
            @PathVariable String id,
            @AuthenticationPrincipal AdministradorData administradorData) {

        if (administradorData == null) {
            throw new AccessDeniedException("Acesso negado. Apenas administradores podem inativar comerciantes.");
        }

        Comerciante comercianteInativado = administradorService.inativarComerciante(id);
        return ResponseEntity.ok(comercianteInativado);
    }

    @PostMapping("/criar-comunicado")
    public ResponseEntity<ComunicadoDTO> criar(@RequestBody ComunicadoDTO dto) {
        ComunicadoDTO criado = comunicadoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/comerciante/all")
    public ResponseEntity<PaginacaoDTO<ComercianteRespostaDTO>> listarTodosComerciantes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String nomeFiltro
    ) {
        PaginacaoDTO<Comerciante> pagina = comercianteService
                .listarComFiltro(nomeFiltro, page, size, sortBy, direction);

        List<ComercianteRespostaDTO> listaDTO = pagina.getConteudo().stream().map(c -> {
            ComercianteRespostaDTO dto = new ComercianteRespostaDTO();
            dto.setId(c.getId());
            dto.setNome(c.getNome());
            dto.setCpf(c.getCpf());
            dto.setEmail(c.getEmail());
            dto.setDataNascimento(c.getDataNascimento());
            dto.setTelefone(c.getTelefone());
            dto.setGenero(c.getGenero());
            dto.setLgpdAccepted(c.getLgpdAccepted());
            dto.setStatus(c.getStatus());
            dto.setConta(c.getConta());
            return dto;
        }).toList();

        PaginacaoDTO<ComercianteRespostaDTO> paginaDTO =
                new PaginacaoDTO<>(
                        listaDTO,
                        pagina.getPaginaAtual(),
                        pagina.getTotalPaginas(),
                        pagina.getTotalElementos(),
                        pagina.getTamanhoPagina(),
                        pagina.isUltimaPagina()
                );

        return ResponseEntity.ok(paginaDTO);
    }

    @GetMapping("/beneficiario/all")
    public ResponseEntity<PaginacaoDTO<BeneficiarioRespostaDTO>> listarTodosBeneficiarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String nomeFiltro
    ) {
        PaginacaoDTO<Beneficiario> pagina = beneficiarioService
                .listarComFiltro(nomeFiltro, page, size, sortBy, direction);

        List<BeneficiarioRespostaDTO> listaDTO = pagina.getConteudo().stream().map(b -> {
            BeneficiarioRespostaDTO dto = new BeneficiarioRespostaDTO();
            dto.setId(b.getId());
            dto.setNome(b.getNome());
            dto.setCpf(b.getCpf());
            dto.setEmail(b.getEmail());
            dto.setDataNascimento(b.getDataNascimento());
            dto.setTelefone(b.getTelefone());
            dto.setGenero(b.getGenero());
            dto.setLgpdAccepted(b.getLgpdAccepted());
            dto.setNumeroCadastroSocial(b.getNumeroCadastroSocial());
            dto.setConta(b.getConta());
            dto.setStatus(b.getStatus());

            if (b.getEndereco() != null) {
                EnderecoRespostaDTO e = new EnderecoRespostaDTO();
                e.setId(b.getEndereco().getId());
                e.setCep(b.getEndereco().getCep());
                e.setLogradouro(b.getEndereco().getLogradouro());
                e.setNumero(b.getEndereco().getNumero());
                e.setBairro(b.getEndereco().getBairro());
                e.setMunicipio(b.getEndereco().getMunicipio());
                dto.setEndereco(e);
            }

            return dto;
        }).toList();

        PaginacaoDTO<BeneficiarioRespostaDTO> paginaDTO =
                new PaginacaoDTO<>(
                        listaDTO,
                        pagina.getPaginaAtual(),
                        pagina.getTotalPaginas(),
                        pagina.getTotalElementos(),
                        pagina.getTamanhoPagina(),
                        pagina.isUltimaPagina()
                );

        return ResponseEntity.ok(paginaDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<AdministradorRespostaDTO> obterAdministradorLogado(
            @AuthenticationPrincipal AdministradorData administradorData) {

        String adminId = administradorData.getAdministrador().getId();
        AdministradorRespostaDTO dto = administradorService.buscarPorIdDto(adminId);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/compras/all")
    public ResponseEntity<List<CompraRespostaDTO>> verTodasAsCompras() {
        List<CompraRespostaDTO> compras = administradorService.verTodasAsCompras();
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/compras/beneficiario/{beneficiarioId}")
    public ResponseEntity<List<Compra>> verComprasPorBeneficiarioId(@PathVariable String beneficiarioId) {
        List<Compra> compras = administradorService.verComprasPorBeneficiarioId(beneficiarioId);
        if (compras.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/compras/estabelecimento/{estabelecimentoId}")
    public ResponseEntity<List<Compra>> verComprasPorEstabelecimentoId(@PathVariable String estabelecimentoId) {
        List<Compra> compras = administradorService.verComprasPorEstabelecimentoId(estabelecimentoId);
        if (compras.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/compras/comerciante/{comercianteId}")
    public ResponseEntity<List<Compra>> verComprasPorComercianteId(@PathVariable String comercianteId) {
        List<Compra> compras = administradorService.verComprasPorComercianteId(comercianteId);
        if (compras.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(compras);
    }

    @PatchMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(
            @RequestBody @Valid AlterarSenhaRequest request,
            Authentication authentication
    ) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof Administrador administradorLogado) {
            administradorService.alterarSenha(administradorLogado.getId(), request);
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário logado não é um Administrador");
        }
    }

}
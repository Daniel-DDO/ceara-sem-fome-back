package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.dto.*;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.security.JWTUtil;
import com.ceara_sem_fome_back.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
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

    // Ativar conta (administrador)
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Pessoa> ativar(@PathVariable AlterarStatusRequest request) {
        Pessoa pessoa = administradorService.alterarStatusAdministrador(request);
        return ResponseEntity.ok(pessoa);
    }

    // Desativar conta (administrador)
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Pessoa> desativarAdm(@PathVariable AlterarStatusRequest request) {
        Pessoa pessoa = administradorService.alterarStatusAdministrador(request);
        return ResponseEntity.ok(pessoa);
    }

    // Desativar conta (qualquer tipo)
    @PatchMapping("/desativar/{tipo}/{id}")
    public ResponseEntity<Pessoa> desativar(@PathVariable AlterarStatusRequest request ) {
        request.setNovoStatusPessoa(StatusPessoa.INATIVO);
        Pessoa pessoa = administradorService.alterarStatusAdministrador(request);
        return ResponseEntity.ok(pessoa);
    }

    // Bloquear conta (qualquer tipo)
    @PatchMapping("/bloquear/{tipo}/{id}")
    public ResponseEntity<Pessoa> bloquear(@PathVariable AlterarStatusRequest request) {
        request.setNovoStatusPessoa(StatusPessoa.BLOQUEADO);
        Pessoa pessoa = administradorService.alterarStatusAdministrador(request);
        return ResponseEntity.ok(pessoa);
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

        //1. Pega o e-mail do usuário logado (armazenado no token)
        String userEmail = principal.getName();

        //2. Chama o novo serviço de atualização
        Administrador adminAtualizado = administradorService.atualizarAdministrador(userEmail, dto);

        //3. A senha não retorna no JSON.
        adminAtualizado.setSenha(null);

        //4. Retorna o objeto atualizado
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
                        est.getTelefone(),                  // comércio
                        est.getEndereco().getLogradouro(),
                        est.getEndereco().getNumero(),
                        est.getEndereco().getBairro(),
                        est.getEndereco().getMunicipio()
                ))
                .toList();

        return ResponseEntity.ok(resposta);
    }

    // endpoints para listagem de compras
    @GetMapping("/compras")
    public ResponseEntity<List<CompraRespostaDTO>> listarTodasCompras() {
        return ResponseEntity.ok(administradorService.listarTodasCompras());
    }

    @GetMapping("/beneficiario/{id}")
    public ResponseEntity<List<CompraRespostaDTO>> listarPorBeneficiario(@PathVariable String id) {
        return ResponseEntity.ok(administradorService.listarComprasPorBeneficiario(id));
    }

    @GetMapping("/estabelecimento/{id}")
    public ResponseEntity<List<CompraRespostaDTO>> listarPorEstabelecimento(@PathVariable String id) {
        return ResponseEntity.ok(administradorService.listarComprasPorEstabelecimento(id));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<CompraRespostaDTO>> listarPorPeriodo(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fim
    ) {
        return ResponseEntity.ok(administradorService.listarComprasPorPeriodo(inicio, fim));
    }

    @GetMapping("/estabelecimento/{id}/status/{status}")
    public ResponseEntity<List<CompraRespostaDTO>> listarPorEstabelecimentoEStatus(
            @PathVariable String id, @PathVariable StatusCompra status) {

        return ResponseEntity.ok(administradorService.listarComprasPorEstabelecimentoEStatus(id, status));
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

    @GetMapping("/compra/all")
    public ResponseEntity<PaginacaoDTO<CompraRespostaDTO>> listarTodasCompras(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataHoraCompra") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String beneficiarioFiltro
    ) {
        PaginacaoDTO<Compra> pagina = compraService
                .listarComFiltro(beneficiarioFiltro, page, size, sortBy, direction);

        List<CompraRespostaDTO> listaDTO = pagina.getConteudo().stream().map(compra -> {

            List<CompraItemDTO> itensDTO = compra.getItens().stream().map(item ->
                    new CompraItemDTO(
                            item.getProduto().getNome(),
                            item.getQuantidade(),
                            item.getPrecoUnitario()
                    )
            ).toList();

            CompraRespostaDTO dto = new CompraRespostaDTO(
                    compra.getId(),
                    compra.getDataHoraCompra(),
                    compra.getValorTotal(),
                    compra.getBeneficiario().getNome(),
                    compra.getEstabelecimento().getNome(),
                    compra.getEndereco() != null
                            ? compra.getEndereco().getLogradouro() + ", " +
                            compra.getEndereco().getNumero() + " - " +
                            compra.getEndereco().getBairro()
                            : null,
                    itensDTO
            );

            dto.setAvaliacao(compra.getAvaliacao());
            return dto;

        }).toList();

        PaginacaoDTO<CompraRespostaDTO> paginaDTO =
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

}
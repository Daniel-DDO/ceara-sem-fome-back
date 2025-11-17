package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.EnderecoCadRequest;
import com.ceara_sem_fome_back.dto.EstabelecimentoRequest;
//import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoEstabDTO;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Estabelecimento;
// import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.service.ComercianteService;
import com.ceara_sem_fome_back.service.EnderecoService;
import com.ceara_sem_fome_back.service.EstabelecimentoService;
import com.ceara_sem_fome_back.service.ProdutoEstabelecimentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/estabelecimento"})
public class EstabelecimentoController {

    @Autowired
    private EstabelecimentoService estabelecimentoService;

    @Autowired
    private ComercianteService comercianteService;

    @Autowired
    private EnderecoService enderecoService;

    @Autowired
    private ProdutoEstabelecimentoService produtoEstabelecimentoService;

    @PostMapping("/cadastrar")
    public ResponseEntity<Estabelecimento> cadastrarEstabelecimento(@Valid @RequestBody EstabelecimentoRequest request) {
        ComercianteData comercianteData =
                (ComercianteData) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String email = comercianteData.getUsername();
        Comerciante comercianteLogado = comercianteService.buscarPorEmail(email);

        Estabelecimento novoEstabelecimento = new Estabelecimento();
        novoEstabelecimento.setNome(request.getNome());
        novoEstabelecimento.setCnpj(request.getCnpj());
        novoEstabelecimento.setTelefone(request.getTelefone());
        novoEstabelecimento.setDataCadastro(LocalDateTime.now());

        Estabelecimento salvo = estabelecimentoService.salvarEstabelecimento(
                novoEstabelecimento,
                comercianteLogado,
                request.getEnderecoCadRequest()
        );

        return ResponseEntity.status(201).body(salvo);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Estabelecimento>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "") String nomeFiltro
    ) {
        Page<Estabelecimento> pagina = estabelecimentoService.listarComFiltro(nomeFiltro, page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/bairro/{bairro}")
    public ResponseEntity<List<Estabelecimento>> listarPorBairro(@PathVariable String bairro) {
        return ResponseEntity.ok(estabelecimentoService.buscarPorBairro(bairro));
    }

    @GetMapping("/municipio/{municipio}")
    public ResponseEntity<List<Estabelecimento>> listarPorMunicipio(@PathVariable String municipio) {
        return ResponseEntity.ok(estabelecimentoService.buscarPorMunicipio(municipio));
    }

    @PostMapping("/cadastrar-endereco/{id}")
    public ResponseEntity<Estabelecimento> cadastrarEndereco(
            @PathVariable String id,
            @Valid @RequestBody EnderecoCadRequest enderecoCadRequest) {

        Estabelecimento estabelecimentoAtualizado = enderecoService.cadastrarEnderecoEstab(id, enderecoCadRequest);
        return ResponseEntity.ok(estabelecimentoAtualizado);
    }

    @GetMapping("/{idEstabelecimento}/produtos")
    public ResponseEntity<?> listarProdutosPorEstabelecimento(
            @PathVariable String idEstabelecimento,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nomeProduto") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        PaginacaoDTO<ProdutoEstabDTO> resultado =
                produtoEstabelecimentoService.listarPorEstabelecimento(idEstabelecimento, page, size, sortBy, direction);

        return ResponseEntity.ok(resultado);
    }

}

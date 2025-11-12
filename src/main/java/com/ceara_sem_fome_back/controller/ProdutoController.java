package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoCadastroRequest;
import com.ceara_sem_fome_back.dto.ProdutoDTO;
import com.ceara_sem_fome_back.exception.AcessoNaoAutorizadoException; // Import adicionado
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException; // Import adicionado
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Produto;
import com.ceara_sem_fome_back.model.ProdutoEstabelecimento;
import com.ceara_sem_fome_back.service.ProdutoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType; // Import adicionado
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException; // Import adicionado
import java.net.URI;
import java.util.List;
import java.util.Map;

@Controller
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping(
            value = "/cadastrar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> cadastrarProduto(
            @RequestPart("produto") @Valid ProdutoDTO produtoDTO,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem,
            @AuthenticationPrincipal ComercianteData comercianteData) {

        Comerciante comercianteLogado = comercianteData.getComerciante();

        try {
            Produto produtoSalvo = produtoService.cadastrarProduto(produtoDTO, comercianteLogado, imagem);

            ProdutoDTO dto = new ProdutoDTO(
                    produtoSalvo.getId(),
                    produtoSalvo.getNome(),
                    produtoSalvo.getLote(),
                    produtoSalvo.getDescricao(),
                    produtoSalvo.getPreco(),
                    produtoSalvo.getQuantidadeEstoque(),
                    produtoSalvo.getStatus(),
                    produtoSalvo.getCategoriaProduto(),
                    produtoSalvo.getImagem(),
                    produtoSalvo.getTipoImagem(),
                    produtoSalvo.getComerciante() != null ? produtoSalvo.getComerciante().getId() : null
            );

            return ResponseEntity.status(201).body(dto);

        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
        } catch (AcessoNaoAutorizadoException e) {
            return ResponseEntity.status(403).body(Map.of("erro", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("erro", "Erro ao processar a imagem."));
        }
    }

    @GetMapping("/estabelecimento/{estabelecimentoId}")
    public ResponseEntity<Page<ProdutoEstabelecimento>> listarProdutosComFiltro(
            @PathVariable String estabelecimentoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "produto.nome") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "") String nomeFiltro
    ) {
        Page<ProdutoEstabelecimento> pagina = produtoService.listarProdutosComFiltroPorEstabelecimento(
                estabelecimentoId,
                nomeFiltro,
                page,
                size,
                sortBy,
                direction
        );
        return ResponseEntity.ok(pagina);
    }

    //sobre o produto
    @PostMapping("/aprovar")
    public ResponseEntity<ProdutoDTO> aprovarProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoAprov = produtoService.aprovarProduto(id);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoAprov.getId(), produtoAprov.getNome(), produtoAprov.getLote(),
                produtoAprov.getDescricao(), produtoAprov.getPreco(), produtoAprov.getQuantidadeEstoque(), produtoAprov.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @PostMapping("/recusar")
    public ResponseEntity<ProdutoDTO> recusarProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoRec = produtoService.recusarProduto(id);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoRec.getId(), produtoRec.getNome(), produtoRec.getLote(),
                produtoRec.getDescricao(), produtoRec.getPreco(), produtoRec.getQuantidadeEstoque(), produtoRec.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @PostMapping("/editar")
    public ResponseEntity<ProdutoDTO> editarProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoEdit = produtoService.editarProduto(id, produtoDTO.getNome(), produtoDTO.getLote(),
                produtoDTO.getDescricao(), produtoDTO.getPreco(), produtoDTO.getQuantidadeEstoque());

        ProdutoDTO prodRespota = new ProdutoDTO(produtoEdit.getId(), produtoEdit.getNome(), produtoEdit.getLote(),
                produtoEdit.getDescricao(), produtoEdit.getPreco(), produtoEdit.getQuantidadeEstoque(), produtoEdit.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @PostMapping("/remover")
    public ResponseEntity<ProdutoDTO> removerProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoRemov = produtoService.removerProduto(id);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoRemov.getId(), produtoRemov.getNome(), produtoRemov.getLote(),
                produtoRemov.getDescricao(), produtoRemov.getPreco(), produtoRemov.getQuantidadeEstoque(), produtoRemov.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @GetMapping("/all")
    public ResponseEntity<PaginacaoDTO<Produto>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String nomeFiltro
    ) {
        PaginacaoDTO<Produto> pagina = produtoService.listarComFiltro(nomeFiltro, page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }


}
package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoDTO;

import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestPart("produto") String produtoJson,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem,
            @AuthenticationPrincipal ComercianteData comercianteData) {

        Comerciante comercianteLogado = comercianteData.getComerciante();

        try {
            ObjectMapper mapper = new ObjectMapper();
            ProdutoDTO produtoDTO = mapper.readValue(produtoJson, ProdutoDTO.class);

            Produto produtoSalvo = produtoService.cadastrarProduto(produtoDTO, comercianteLogado, imagem);

            ProdutoDTO dto = new ProdutoDTO(
                    produtoSalvo.getId(),
                    produtoSalvo.getNome(),
                    produtoSalvo.getLote(),
                    produtoSalvo.getDescricao(),
                    produtoSalvo.getPreco(),
                    produtoSalvo.getQuantidadeEstoque(),
                    produtoSalvo.getStatus(),
                    produtoSalvo.getCategoria(),
                    produtoSalvo.getUnidade(),
                    produtoSalvo.getImagem(),
                    produtoSalvo.getTipoImagem(),
                    produtoSalvo.getAvaliadoPorId(),
                    produtoSalvo.getComerciante() != null ? produtoSalvo.getComerciante().getId() : null
            );

            return ResponseEntity.status(201).body(dto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao processar a imagem ou o JSON do produto."));
        }
    }

    @PutMapping(
            value = "/editar/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> editarProduto(
            @PathVariable String id,
            @RequestPart("produto") String produtoJson,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            ProdutoDTO produtoDTO = mapper.readValue(produtoJson, ProdutoDTO.class);
            produtoDTO.setId(id);

            Produto produtoEdit = produtoService.editarProdutoComImagem(produtoDTO, imagem);

            ProdutoDTO dto = new ProdutoDTO(
                    produtoEdit.getId(),
                    produtoEdit.getNome(),
                    produtoEdit.getLote(),
                    produtoEdit.getDescricao(),
                    produtoEdit.getPreco(),
                    produtoEdit.getQuantidadeEstoque(),
                    produtoEdit.getStatus(),
                    produtoEdit.getCategoria(),
                    produtoEdit.getUnidade(),
                    produtoEdit.getImagem(),
                    produtoEdit.getTipoImagem(),
                    produtoEdit.getAvaliadoPorId(),
                    produtoEdit.getComerciante() != null ? produtoEdit.getComerciante().getId() : null
            );

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao atualizar o produto ou processar a imagem."));
        }
    }

    @PostMapping("/remover")
    public ResponseEntity<ProdutoDTO> removerProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoRemov = produtoService.removerProduto(id);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoRemov.getId(), produtoRemov.getNome(), produtoRemov.getLote(),
                produtoRemov.getDescricao(), produtoRemov.getPreco(), produtoRemov.getQuantidadeEstoque(), produtoRemov.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @PatchMapping("/aprovar/{id}")
    public ResponseEntity<ProdutoDTO> aprovarProduto(
            @RequestBody ProdutoDTO produtoDTO,
            @AuthenticationPrincipal AdministradorData administradorData) {

        Administrador administradorLogado = administradorData.getAdministrador();

        String id = produtoDTO.getId();
        Produto produtoAprov = produtoService.aprovarProduto(id, administradorLogado);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoAprov.getId(),
                produtoAprov.getNome(),
                produtoAprov.getLote(),
                produtoAprov.getDescricao(),
                produtoAprov.getPreco(),
                produtoAprov.getQuantidadeEstoque(),
                produtoAprov.getStatus(),
                produtoAprov.getCategoria(),
                produtoAprov.getUnidade(),
                produtoAprov.getImagem(),
                produtoAprov.getTipoImagem(),
                produtoAprov.getAvaliadoPorId(),
                produtoAprov.getComerciante().getId()
        );

        return ResponseEntity.ok(prodRespota);
    }

    @PatchMapping("/recusar/{id}")
    public ResponseEntity<ProdutoDTO> recusarProduto(
            @RequestBody ProdutoDTO produtoDTO,
            @AuthenticationPrincipal AdministradorData administradorData) {

        Administrador administradorLogado = administradorData.getAdministrador();

        String id = produtoDTO.getId();
        Produto produtoRec = produtoService.recusarProduto(id, administradorLogado);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoRec.getId(),
                produtoRec.getNome(),
                produtoRec.getLote(),
                produtoRec.getDescricao(),
                produtoRec.getPreco(),
                produtoRec.getQuantidadeEstoque(),
                produtoRec.getStatus(),
                produtoRec.getCategoria(),
                produtoRec.getUnidade(),
                produtoRec.getImagem(),
                produtoRec.getTipoImagem(),
                produtoRec.getAvaliadoPorId(),
                produtoRec.getComerciante().getId()
        );

        return ResponseEntity.ok(prodRespota);
    }

    @GetMapping("/categorias")
    public ResponseEntity<CategoriaProduto[]> listarCategorias() {
        return ResponseEntity.ok(CategoriaProduto.values());
    }

    @GetMapping("/unidades")
    public ResponseEntity<UnidadeProduto[]> listarUnidades() {
        return ResponseEntity.ok(UnidadeProduto.values());
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
package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.ComercianteData;
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

@Controller
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // --- METODO MODIFICADO ---
    @PostMapping(value = "/cadastrar", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> cadastrarProduto(
            @ModelAttribute @Valid ProdutoCadastroRequest request, // Trocado para @ModelAttribute
            @RequestParam(value = "file", required = false) MultipartFile file, // Recebe o arquivo
            @AuthenticationPrincipal ComercianteData comercianteData) {

        Comerciante comercianteLogado = comercianteData.getComerciante();

        try {
            ProdutoEstabelecimento associacaoSalva = produtoService.cadastrarProduto(request, comercianteLogado, file);
            return ResponseEntity.status(201).body(associacaoSalva);
        
        // Adicionando tratamento de exceções que o service pode lançar
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (AcessoNaoAutorizadoException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erro ao processar a imagem: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // --- FIM DA MODIFICAÇÃO ---

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

    //imagens

    @PostMapping(value = "/{id}/imagem", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadImagem(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        try {
            Produto atualizado = produtoService.salvarImagem(id, file);
            //Retorna 200 OK com header Location apontando para endpoint de imagem
            URI uri = URI.create("/produtos/" + id + "/imagem");
            return ResponseEntity.ok()
                    .header(HttpHeaders.LOCATION, uri.toString())
                    .body("Imagem salva com sucesso.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    //imagem: retorna bytes e Content-Type correto
    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> getImagem(@PathVariable String id) {
        try {
            byte[] imagem = produtoService.buscarImagem(id);
            if (imagem == null || imagem.length == 0) {
                return ResponseEntity.notFound().build();
            }
            String tipo = produtoService.buscarTipoImagem(id);
            MediaType mediaType;
            try {
                mediaType = (tipo != null) ? MediaType.parseMediaType(tipo) : MediaType.APPLICATION_OCTET_STREAM;
            } catch (Exception ex) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(imagem);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Deleta imagem do produto (mantém produto)
    @DeleteMapping("/{id}/imagem")
    public ResponseEntity<?> deleteImagem(@PathVariable String id) {
        try {
            produtoService.removerImagem(id);
            return ResponseEntity.ok("Imagem removida com sucesso.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao remover imagem: " + e.getMessage());
        }
    }

    //informa se o produto possui imagem e qual o tipo
    @GetMapping("/{id}/imagem/metadata")
    public ResponseEntity<?> metadataImagem(@PathVariable String id) {
        try {
            boolean possui = produtoService.possuiImagem(id);
            String tipo = produtoService.buscarTipoImagem(id);
            return ResponseEntity.ok(new Object() {
                public final boolean existe = possui;
                public final String tipoMime = tipo;
            });
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
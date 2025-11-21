package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.data.BeneficiarioData;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.*;

import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.service.ProdutoEstabelecimentoService;
import com.ceara_sem_fome_back.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoEstabelecimentoService produtoEstabelecimentoService;

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
                    produtoSalvo.getDataCadastro(),
                    produtoSalvo.getAvaliadoPorId(),
                    produtoSalvo.getDataAvaliacao(),
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
                    produtoEdit.getDataCadastro(),
                    produtoEdit.getAvaliadoPorId(),
                    produtoEdit.getDataAvaliacao(),
                    produtoEdit.getComerciante() != null ? produtoEdit.getComerciante().getId() : null
            );

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao atualizar o produto ou processar a imagem."));
        }
    }

    @PatchMapping("/atualizar-estoque/{id}")
    public ResponseEntity<?> atualizarEstoque(
            @PathVariable String id,
            @RequestBody AtualizarEstoqueDTO dto,
            @AuthenticationPrincipal ComercianteData comercianteData
    ) {
        try {
            Comerciante comercianteAutenticado = comercianteData.getComerciante();

            Produto produtoAtualizado = produtoService.atualizarEstoque(id, dto, comercianteAutenticado);

            ProdutoDTO resposta = new ProdutoDTO(
                    produtoAtualizado.getId(),
                    produtoAtualizado.getNome(),
                    produtoAtualizado.getLote(),
                    produtoAtualizado.getDescricao(),
                    produtoAtualizado.getPreco(),
                    produtoAtualizado.getQuantidadeEstoque(),
                    produtoAtualizado.getStatus(),
                    produtoAtualizado.getCategoria(),
                    produtoAtualizado.getUnidade(),
                    produtoAtualizado.getImagem(),
                    produtoAtualizado.getTipoImagem(),
                    produtoAtualizado.getDataCadastro(),
                    produtoAtualizado.getAvaliadoPorId(),
                    produtoAtualizado.getDataAvaliacao(),
                    produtoAtualizado.getComerciante() != null ?
                            produtoAtualizado.getComerciante().getId() : null
            );

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao atualizar o estoque do produto."));
        }
    }

    @DeleteMapping("/remover/{id}")
    public ResponseEntity<?> removerProduto(@PathVariable String id) {
        try {
            Produto produto = produtoService.removerProduto(id);

            ProdutoDTO resposta = new ProdutoDTO(
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
                    produto.getComerciante() != null ? produto.getComerciante().getId() : null
            );

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao remover o produto."));
        }
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

    @GetMapping("/produtos/filtrar")
    public ResponseEntity<List<Produto>> filtrar(@RequestParam String pesquisa) {
        List<Produto> produtos = produtoService.filtrarProdutosPorNome(pesquisa);
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/estabelecimento/all")
    public ResponseEntity<PaginacaoDTO<ProdutoEstabDTO>> listarTodosProdEst(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String nomeFiltro,
            @AuthenticationPrincipal BeneficiarioData beneficiarioData
    ) {
        if (beneficiarioData == null || beneficiarioData.getBeneficiario() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PaginacaoDTO<ProdutoEstabDTO> pagina =
                produtoEstabelecimentoService.listarComFiltro(nomeFiltro, page, size, sortBy, direction);

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        Produto produto = produtoService.buscarPorId(id);

        if (produto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Produto não encontrado");
        }

        ProdutoDTO dto = new ProdutoDTO();
        dto.setId(produto.getId());
        dto.setNome(produto.getNome());
        dto.setLote(produto.getLote());
        dto.setDescricao(produto.getDescricao());
        dto.setPreco(produto.getPreco());
        dto.setQuantidadeEstoque(produto.getQuantidadeEstoque());
        dto.setStatus(produto.getStatus());
        dto.setCategoria(produto.getCategoria());
        dto.setUnidade(produto.getUnidade());
        dto.setImagem(produto.getImagem());
        dto.setTipoImagem(produto.getTipoImagem());
        dto.setComercianteId(produto.getComerciante().getId());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/estabelecimento/{idProduto}")
    public ResponseEntity<?> buscarProdutoEstabelecimentoPorId(@PathVariable String idProduto) {

        ProdutoEstabelecimento pe = produtoEstabelecimentoService.buscarPorProdutoId(idProduto);

        if (pe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Produto não encontrado ou não vinculado a estabelecimento.");
        }

        Produto produto = pe.getProduto();
        Estabelecimento estabelecimento = pe.getEstabelecimento();

        ProdutoEstabDTO dto = new ProdutoEstabDTO();



        return ResponseEntity.ok(dto);
    }

    @GetMapping("/estabelecimento/pe/{idProdEstab}")
    public ResponseEntity<?> buscarPorIdProdEstab(@PathVariable String idProdEstab) {

        ProdutoEstabelecimento pe = produtoEstabelecimentoService.buscarPorId(idProdEstab);

        if (pe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ProdutoEstabelecimento não encontrado.");
        }

        ProdutoEstabDTO dto = new ProdutoEstabDTO();

        Produto p = pe.getProduto();
        Estabelecimento e = pe.getEstabelecimento();

        ProdutoDTO produtoDto = new ProdutoDTO();
        EstabelecimentoRespostaDTO estabelecimentoDto = new EstabelecimentoRespostaDTO();

        produtoDto.setId(p.getId());
        produtoDto.setNome(p.getNome());
        produtoDto.setLote(p.getLote());
        produtoDto.setDescricao(p.getDescricao());
        produtoDto.setPreco(p.getPreco());
        produtoDto.setQuantidadeEstoque(p.getQuantidadeEstoque());
        produtoDto.setStatus(p.getStatus());
        produtoDto.setCategoria(p.getCategoria());
        produtoDto.setUnidade(p.getUnidade());
        produtoDto.setImagem(p.getImagem());
        produtoDto.setTipoImagem(p.getTipoImagem());
        produtoDto.setDataCadastro(p.getDataCadastro());
        produtoDto.setAvaliadoPorId(p.getAvaliadoPorId());
        produtoDto.setDataAvaliacao(p.getDataAvaliacao());
        produtoDto.setComercianteId(p.getComerciante().getId());

        estabelecimentoDto.setId(e.getId());
        estabelecimentoDto.setNome(e.getNome());
        estabelecimentoDto.setCnpj(e.getCnpj());
        estabelecimentoDto.setTelefone(e.getTelefone());
        estabelecimentoDto.setImagem(e.getImagem());
        estabelecimentoDto.setTipoImagem(e.getTipoImagem());
        estabelecimentoDto.setEnderecoId(e.getEndereco().getId());
        estabelecimentoDto.setCep(e.getEndereco().getCep());
        estabelecimentoDto.setLogradouro(e.getEndereco().getLogradouro());
        estabelecimentoDto.setNumero(e.getEndereco().getNumero());
        estabelecimentoDto.setBairro(e.getEndereco().getBairro());
        estabelecimentoDto.setMunicipio(e.getEndereco().getMunicipio());

        dto.setId(pe.getId());
        dto.setProdutoDTO(produtoDto);
        dto.setEstabelecimentoRespostaDTO(estabelecimentoDto);

        return ResponseEntity.ok(dto);
    }


}
/*
package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.AdministradorData;
import com.ceara_sem_fome_back.data.ComercianteData;
import com.ceara_sem_fome_back.dto.AtualizarEstoqueDTO;
import com.ceara_sem_fome_back.dto.PaginacaoDTO;
import com.ceara_sem_fome_back.dto.ProdutoDTO;

import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.service.ProdutoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
    @PreAuthorize("hasRole('ROLE_COMERCIANTE')")
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
                    produtoSalvo.getDataCadastro(),
                    produtoSalvo.getAvaliadoPorId(),
                    produtoSalvo.getDataAvaliacao(),
                    produtoSalvo.getComerciante() != null ? produtoSalvo.getComerciante().getId() : null
            );

            return ResponseEntity.status(201).body(dto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao processar a imagem ou o JSON do produto."));
        }
    }

    @PatchMapping("/{id}/estoque")
    @PreAuthorize("hasRole('ROLE_COMERCIANTE')")
    public ResponseEntity<ProdutoDTO> atualizarEstoque(
            @PathVariable String id,
            @RequestBody AtualizarEstoqueDTO dto,
            @AuthenticationPrincipal ComercianteData comercianteData) {

        Comerciante comercianteLogado = comercianteData.getComerciante();
        Produto produtoAtualizado = produtoService.atualizarEstoque(id, dto, comercianteLogado);

        ProdutoDTO produtoResposta = new ProdutoDTO(
                produtoAtualizado.getId(),
                produtoAtualizado.getNome(),
                produtoAtualizado.getLote(),
                produtoAtualizado.getDescricao(),
                produtoAtualizado.getPreco(),
                produtoAtualizado.getQuantidadeEstoque(),
                produtoAtualizado.getStatus(),
                produtoAtualizado.getCategoria(),
                produtoAtualizado.getUnidade(),
                produtoAtualizado.getImagem(),
                produtoAtualizado.getTipoImagem(),
                produtoAtualizado.getDataCadastro(),
                produtoAtualizado.getAvaliadoPorId(),
                produtoAtualizado.getDataAvaliacao(),
                produtoAtualizado.getComerciante().getId()
        );

        return ResponseEntity.ok(produtoResposta);
    }

    @PutMapping(
            value = "/editar/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ROLE_COMERCIANTE')")
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
                    produtoEdit.getDataCadastro(),
                    produtoEdit.getAvaliadoPorId(),
                    produtoEdit.getDataAvaliacao(),
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
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<ProdutoDTO> removerProduto(@RequestBody ProdutoDTO produtoDTO) {
        String id = produtoDTO.getId();
        Produto produtoRemov = produtoService.removerProduto(id);

        ProdutoDTO prodRespota = new ProdutoDTO(produtoRemov.getId(), produtoRemov.getNome(), produtoRemov.getLote(),
                produtoRemov.getDescricao(), produtoRemov.getPreco(), produtoRemov.getQuantidadeEstoque(), produtoRemov.getStatus());

        return ResponseEntity.ok(prodRespota);
    }

    @PatchMapping("/aprovar/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
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
                produtoAprov.getDataCadastro(),
                produtoAprov.getAvaliadoPorId(),
                produtoAprov.getDataAvaliacao(),
                produtoAprov.getComerciante().getId()
        );

        return ResponseEntity.ok(prodRespota);
    }

    @PatchMapping("/recusar/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
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
                produtoRec.getDataCadastro(),
                produtoRec.getAvaliadoPorId(),
                produtoRec.getDataAvaliacao(),
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

    @GetMapping("/produtos/filtrar")
    public ResponseEntity<List<Produto>> filtrar(@RequestParam String pesquisa) {
        List<Produto> produtos = produtoService.filtrarProdutosPorNome(pesquisa);
        return ResponseEntity.ok(produtos);
    }
}

 */
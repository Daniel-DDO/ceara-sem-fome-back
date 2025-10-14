package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.dto.EstabelecimentoRequest;
import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.service.EstabelecimentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping({"/estabelecimento"})
public class EstabelecimentoController {

    @Autowired
    private EstabelecimentoService estabelecimentoService;

@PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarEstabelecimento(@RequestBody @Valid EstabelecimentoRequest request) {
        try {
            Estabelecimento novoEstabelecimento = new Estabelecimento();
            novoEstabelecimento.setId(request.getId());
            novoEstabelecimento.setNome(request.getNome());

            Estabelecimento estabelecimentoSalvo = estabelecimentoService.salvarEstabelecimento(novoEstabelecimento);

            return ResponseEntity.status(201).body(estabelecimentoSalvo);

        } catch (IllegalArgumentException e) {
            // Erros de regra de negócio (ex: ID duplicado)
            ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), 400);
            return ResponseEntity.badRequest().body(errorDTO);

        } catch (Exception e) {
            ErrorDTO errorDTO = new ErrorDTO("Erro interno ao tentar cadastrar o estabelecimento.", 500);
            return ResponseEntity.status(500).body(errorDTO);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Estabelecimento>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Page<Estabelecimento> pagina = estabelecimentoService.listarTodos(page, size, sortBy, direction);
        return ResponseEntity.ok(pagina);
    }
}

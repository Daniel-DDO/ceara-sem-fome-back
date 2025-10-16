package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.EstabelecimentoRequest;
//import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Estabelecimento;
// import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.service.ComercianteService;
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

    @Autowired
    private ComercianteService comercianteService;

@PostMapping("/cadastrar")
    public ResponseEntity<Object> cadastrarEstabelecimento(@RequestBody @Valid EstabelecimentoRequest request) {
    Comerciante comerciante = comercianteService.buscarComerciantePorCpf(request.getComercianteCpf());
            Estabelecimento novoEstabelecimento = new Estabelecimento();
            novoEstabelecimento.setId(request.getId());
            novoEstabelecimento.setNome(request.getNome());

            Estabelecimento estabelecimentoSalvo = estabelecimentoService.salvarEstabelecimento(novoEstabelecimento, comerciante);

            return ResponseEntity.status(201).body(estabelecimentoSalvo);
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

package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.data.dto.ErrorDTO;
import com.ceara_sem_fome_back.dto.EstabelecimentoRequest;
import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.service.EstabelecimentoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/estabelecimento"})
public class EstabelecimentoController {

    @Autowired
    private EstabelecimentoService estabelecimentoService;

    @PostMapping
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
}

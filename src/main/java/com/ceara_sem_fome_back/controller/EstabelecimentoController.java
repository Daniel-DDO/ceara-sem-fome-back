package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.service.EstabelecimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/estabelecimento"})
public class EstabelecimentoController {

    @Autowired
    private EstabelecimentoService estabelecimentoService;
}

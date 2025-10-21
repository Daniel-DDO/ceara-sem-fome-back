package com.ceara_sem_fome_back.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@RequestMapping({"/produto"})
public class ProdutoController {

    @PostMapping("/aprovar")
    public void aprovarProduto() {

    }

    @PostMapping("/recusar")
    public void recusarProduto() {

    }

    @PostMapping("/editar")
    public void editarProduto() {

    }

    @PostMapping("/remover")
    public void removerProduto() {

    }
}

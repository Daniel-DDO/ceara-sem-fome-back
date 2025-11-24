package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.model.Conta;
import com.ceara_sem_fome_back.service.DepositoMensalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/financeiro")
public class ContaFinanceiroController {

    @Autowired
    private DepositoMensalService depositoMensalService;

    @PostMapping("/conta/{contaId}/depositar")
    public Conta depositar(
            @PathVariable String contaId,
            @RequestParam BigDecimal valor
    ) {
        return depositoMensalService.depositarEmConta(contaId, valor);
    }

    @PostMapping("/conta/{contaId}/remover")
    public Conta remover(
            @PathVariable String contaId,
            @RequestParam BigDecimal valor
    ) {
        return depositoMensalService.removerDeConta(contaId, valor);
    }

    @PostMapping("/testar-deposito-mensal")
    public String testarDepositoMensal() {
        depositoMensalService.testarDeposito();
        return "Depósito mensal executado manualmente.";
    }
}

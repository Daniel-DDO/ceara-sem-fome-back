package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Conta;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepositoMensalService {

    private static final BigDecimal VALOR_MENSAL = BigDecimal.valueOf(300.00);

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Scheduled(cron = "0 5 0 1 * *")
    public void depositarParaTodosNoDiaPrimeiro() {
        processarDepositoMensal();
    }

    public void processarDepositoMensal() {
        List<Beneficiario> beneficiarios = beneficiarioRepository.findAll();

        for (Beneficiario b : beneficiarios) {
            Conta c = b.getConta();
            if (c != null && c.isAtiva()) {
                c.setSaldo(c.getSaldo().add(VALOR_MENSAL));
                c.setAtualizadoEm(LocalDateTime.now());
            }
        }

        beneficiarioRepository.saveAll(beneficiarios);
    }

    public void testarDeposito() {
        processarDepositoMensal();
    }

    public Conta depositarEmConta(String contaId, BigDecimal valor) {

        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + contaId));

        if (!conta.isAtiva()) {
            throw new RuntimeException("Conta está desativada.");
        }

        conta.setSaldo(conta.getSaldo().add(valor));
        conta.setAtualizadoEm(LocalDateTime.now());

        return contaRepository.save(conta);
    }

    public Conta removerDeConta(String contaId, BigDecimal valor) {

        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + contaId));

        if (!conta.isAtiva()) {
            throw new RuntimeException("Conta está desativada.");
        }

        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente.");
        }

        conta.setSaldo(conta.getSaldo().subtract(valor));
        conta.setAtualizadoEm(LocalDateTime.now());

        return contaRepository.save(conta);
    }
}

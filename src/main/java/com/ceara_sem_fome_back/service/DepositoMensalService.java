package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Conta;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
import com.ceara_sem_fome_back.repository.ContaRepository;
import lombok.extern.slf4j.Slf4j; // Importante para logs
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DepositoMensalService {

    private static final BigDecimal VALOR_MENSAL = BigDecimal.valueOf(300.00);

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Scheduled(cron = "0 5 0 1 * *")
    public void depositarParaTodosNoDiaPrimeiro() {
        log.info("Iniciando rotina de depósito mensal...");
        processarDepositoMensal();
        log.info("Rotina de depósito mensal finalizada.");
    }

    public void processarDepositoMensal() {
        List<Beneficiario> beneficiarios = beneficiarioRepository.findAll();

        int sucesso = 0;
        int erros = 0;

        for (Beneficiario b : beneficiarios) {
            try {
                realizarDepositoIndividual(b);
                sucesso++;
            } catch (Exception e) {
                log.error("Erro ao depositar para beneficiário ID: " + b.getId(), e);
                erros++;
            }
        }

        log.info("Resumo: {} depósitos realizados, {} falhas.", sucesso, erros);
    }

    @Transactional
    public void realizarDepositoIndividual(Beneficiario b) {
        Conta c = b.getConta();

        if (c != null && c.isAtiva()) {
            c.setSaldo(c.getSaldo().add(VALOR_MENSAL));
            c.setAtualizadoEm(LocalDateTime.now());

            contaRepository.save(c);

            String msgDeposito = String.format("O Governo depositou seu benefício mensal de R$ %.2f! Confira seu saldo.", VALOR_MENSAL);
            notificacaoService.criarEEnviarNotificacao(b.getId(), msgDeposito);
        }
    }
    @Transactional
    public Conta depositarEmConta(String contaId, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo.");
        }

        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + contaId));

        if (!conta.isAtiva()) {
            throw new RuntimeException("Conta está desativada.");
        }

        conta.setSaldo(conta.getSaldo().add(valor));
        conta.setAtualizadoEm(LocalDateTime.now());
        Conta contaSalva = contaRepository.save(conta);

        String msgDeposito = String.format("Você recebeu um depósito de R$ %.2f em sua conta!", valor);

        beneficiarioRepository.findByConta(conta).ifPresentOrElse(
                beneficiario -> {
                    notificacaoService.criarEEnviarNotificacao(beneficiario.getId(), msgDeposito);
                },
                () -> {
                    comercianteRepository.findByConta(conta).ifPresent(comerciante -> {
                        notificacaoService.criarEEnviarNotificacao(comerciante.getId(), msgDeposito);
                    });
                }
        );

        return contaSalva;
    }

    @Transactional
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

    public void testarDeposito() {
        processarDepositoMensal();
    }
}
package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.config.NotificacaoEvent;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Conta;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
import com.ceara_sem_fome_back.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepositoMensalService {

    private static final BigDecimal VALOR_MENSAL = BigDecimal.valueOf(300.00);

    private final BeneficiarioRepository beneficiarioRepository;
    private final ComercianteRepository comercianteRepository;
    private final ContaRepository contaRepository;

    private final ApplicationEventPublisher eventPublisher;

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
                depositarBeneficioMensal(b);
                sucesso++;
            } catch (Exception e) {
                log.error("Erro ao depositar para beneficiário ID: {}", b.getId(), e);
                erros++;
            }
        }

        log.info("Resumo: {} depósitos realizados, {} falhas.", sucesso, erros);
    }

    private void depositarBeneficioMensal(Beneficiario b) {
        Conta c = b.getConta();

        if (c != null && c.isAtiva()) {
            c.setSaldo(c.getSaldo().add(VALOR_MENSAL));
            c.setAtualizadoEm(LocalDateTime.now());
            contaRepository.save(c);

            String msg = String.format("O Governo depositou seu benefício de R$ %.2f! Confira seu saldo.", VALOR_MENSAL);
            eventPublisher.publishEvent(new NotificacaoEvent(this, b.getId(), msg));
        }
    }

    @Transactional
    public Conta depositarEmConta(String contaId, BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo.");
        }

        Conta conta = buscarContaAtiva(contaId);

        conta.setSaldo(conta.getSaldo().add(valor));
        conta.setAtualizadoEm(LocalDateTime.now());
        Conta contaSalva = contaRepository.save(conta);

        // Notificar dono da conta
        notificarDonoDaConta(conta, String.format("Você recebeu um depósito de R$ %.2f.", valor));

        return contaSalva;
    }

    @Transactional
    public Conta removerDeConta(String contaId, BigDecimal valor) {
        Conta conta = buscarContaAtiva(contaId);

        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente para a operação.");
        }

        conta.setSaldo(conta.getSaldo().subtract(valor));
        conta.setAtualizadoEm(LocalDateTime.now());
        Conta contaSalva = contaRepository.save(conta);

        notificarDonoDaConta(conta, String.format("Um débito de R$ %.2f foi realizado na sua conta.", valor));

        return contaSalva;
    }

    private Conta buscarContaAtiva(String contaId) {
        Conta conta = contaRepository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + contaId));

        if (!conta.isAtiva()) {
            throw new RuntimeException("Conta está desativada e não pode ser movimentada.");
        }
        return conta;
    }

    private void notificarDonoDaConta(Conta conta, String mensagem) {
        beneficiarioRepository.findByConta(conta).ifPresentOrElse(
                b -> eventPublisher.publishEvent(new NotificacaoEvent(this, b.getId(), mensagem)),
                () -> {
                    comercianteRepository.findByConta(conta).ifPresent(
                            c -> eventPublisher.publishEvent(new NotificacaoEvent(this, c.getId(), mensagem))
                    );
                }
        );
    }

    public void testarDeposito() {
        log.info("Iniciando teste manual de depósito...");
        processarDepositoMensal();
    }
}
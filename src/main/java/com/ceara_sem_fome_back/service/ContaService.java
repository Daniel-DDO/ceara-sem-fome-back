package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Conta;
import com.ceara_sem_fome_back.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    private final Random random = new Random();

    public void gerarNumeroEAgencia(Conta conta) {
        String agencia = gerarAgencia();
        String numeroConta = gerarNumeroUnico();

        conta.setAgencia(agencia);
        conta.setNumeroConta(numeroConta);
    }

    private String gerarAgencia() {
        return String.format("%04d", random.nextInt(10000));
    }

    private String gerarNumeroUnico() {
        String numero;

        do {
            numero = gerarNumeroConta();
        } while (contaRepository.existsByNumeroConta(numero));

        return numero;
    }

    private String gerarNumeroConta() {

        int tamanho = 5 + random.nextInt(4);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tamanho; i++) {
            sb.append(random.nextInt(10));
        }

        String dv = gerarDV();

        return sb.toString() + "-" + dv;
    }

    private String gerarDV() {
        //10% chance de 'X'
        if (random.nextInt(10) == 0) {
            return "X";
        }
        return String.valueOf(random.nextInt(10));
    }
}


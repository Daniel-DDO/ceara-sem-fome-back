package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.exception.ContaNaoExisteException;
import com.ceara_sem_fome_back.model.Entregador;
import com.ceara_sem_fome_back.repository.EntregadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntregadorService {

    @Autowired
    private EntregadorRepository entregadorRepository;

    public Entregador logarEntregador(String email, String senha) {
        Entregador entregador = entregadorRepository.findByEmail(email);

        if (entregador != null && entregador.getSenha().equals(senha)) {
            return entregador;
        }

        throw new ContaNaoExisteException(email);
    }

    public boolean verificarCpf(String cpf) {
        return PessoaUtils.verificarCpf(cpf);
    }

    public Entregador salvarEntregador(Entregador entregador) {

        if (entregadorRepository.existsById(entregador.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        if (entregadorRepository.findByEmail(entregador.getEmail()) != null) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }
        return entregadorRepository.save(entregador);
    }
}

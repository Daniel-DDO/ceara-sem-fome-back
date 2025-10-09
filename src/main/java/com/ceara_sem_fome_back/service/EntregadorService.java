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

    public Entregador logarEntregador(String login, String senha) {
        Entregador entregador = entregadorRepository.findByLogin(login);

        if (entregador != null && entregador.getSenha().equals(senha)) {
            return entregador;
        }

        throw new ContaNaoExisteException(login);
    }
}

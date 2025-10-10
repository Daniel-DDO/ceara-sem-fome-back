package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EstabelecimentoService {

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    public Estabelecimento salvarEstabelecimento(Estabelecimento estabelecimento) {

        if (estabelecimentoRepository.existsById(estabelecimento.getId())) {
            throw new IllegalArgumentException("Estabelecimento com o ID " + estabelecimento.getId() + " já está cadastrado.");
        }

        return estabelecimentoRepository.save(estabelecimento);
    }

}

package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.exception.EstabelecimentoJaCadastradoException;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class EstabelecimentoService {

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    public Estabelecimento salvarEstabelecimento(Estabelecimento estabelecimento) {

        if (estabelecimentoRepository.existsById(estabelecimento.getId())) {
            throw new EstabelecimentoJaCadastradoException(estabelecimento.getId());
        }

        return estabelecimentoRepository.save(estabelecimento);
    }

    public Page<Estabelecimento> listarTodos(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return estabelecimentoRepository.findAll(pageable);
    }
}
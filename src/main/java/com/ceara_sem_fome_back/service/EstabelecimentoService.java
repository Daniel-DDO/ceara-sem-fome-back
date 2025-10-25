package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.exception.EstabelecimentoJaCadastradoException;
import com.ceara_sem_fome_back.model.Comerciante;
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

    public Estabelecimento salvarEstabelecimento(Estabelecimento estabelecimento, Comerciante comerciante) {

        if (estabelecimentoRepository.existsById(estabelecimento.getId())) {
            throw new EstabelecimentoJaCadastradoException(estabelecimento.getId());
        }

        estabelecimento.setComerciante(comerciante);
        return estabelecimentoRepository.save(estabelecimento);
    }

    public Page<Estabelecimento> listarComFiltro(
            String nomeFiltro,
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Aplica o filtro se for válido
        if (nomeFiltro != null && !nomeFiltro.isBlank()) {
            return estabelecimentoRepository.findByNomeContainingIgnoreCase(nomeFiltro, pageable);
        } else {
            // Sem filtro, apenas paginação
            return estabelecimentoRepository.findAll(pageable);
        }
    }
}
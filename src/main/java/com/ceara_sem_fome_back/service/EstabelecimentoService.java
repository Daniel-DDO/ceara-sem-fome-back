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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstabelecimentoService {

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Transactional
    public Estabelecimento salvarEstabelecimento(Estabelecimento estabelecimento, Comerciante comerciante) {

        if (estabelecimentoRepository.existsByNomeAndComerciante(estabelecimento.getNome(), comerciante)) {
            throw new EstabelecimentoJaCadastradoException(estabelecimento.getNome());
        }

        estabelecimento.setComerciante(comerciante);
        Estabelecimento salvo = estabelecimentoRepository.save(estabelecimento);

        comerciante.getEstabelecimentos().add(salvo);

        return salvo;
    }

    public Page<Estabelecimento> listarTodos(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return estabelecimentoRepository.findAll(pageable);
    }

    public List<Estabelecimento> buscarPorBairro(String bairro) {
        return estabelecimentoRepository.findByEnderecoBairro(bairro);
    }

    public List<Estabelecimento> buscarPorMunicipio(String municipio) {
        return estabelecimentoRepository.findByEnderecoMunicipio(municipio);
    }
}
package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.EnderecoCadRequest;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EstabelecimentoService {

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private EnderecoService enderecoService;

    @Transactional
    public Estabelecimento salvarEstabelecimento(Estabelecimento estabelecimento, Comerciante comerciante, EnderecoCadRequest enderecoCadRequest) {
        if (estabelecimentoRepository.existsByNomeAndComerciante(estabelecimento.getNome(), comerciante)) {
            throw new EstabelecimentoJaCadastradoException(estabelecimento.getNome());
        }

        estabelecimento.setComerciante(comerciante);
        estabelecimento.setDataCadastro(LocalDateTime.now());

        Estabelecimento salvo = estabelecimentoRepository.save(estabelecimento);

        if (enderecoCadRequest != null) {
            salvo = enderecoService.cadastrarEnderecoEstab(salvo.getId(), enderecoCadRequest);
        }

        comerciante.getEstabelecimentos().add(salvo);

        return salvo;
    }

    public Page<Estabelecimento> listarTodos(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return estabelecimentoRepository.findAll(pageable);
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

    public List<Estabelecimento> buscarPorBairro(String bairro) {
        return estabelecimentoRepository.findByEnderecoBairro(bairro);
    }

    public List<Estabelecimento> buscarPorMunicipio(String municipio) {
        return estabelecimentoRepository.findByEnderecoMunicipio(municipio);
    }
}
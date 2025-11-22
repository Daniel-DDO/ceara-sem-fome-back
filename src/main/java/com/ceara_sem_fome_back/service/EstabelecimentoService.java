package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.EnderecoCadRequest;
import com.ceara_sem_fome_back.dto.EstabelecimentoRespostaDTO;
import com.ceara_sem_fome_back.exception.AcessoNaoAutorizadoException;
import com.ceara_sem_fome_back.exception.EstabelecimentoJaCadastradoException;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
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
import java.util.stream.Collectors;

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

    public void verificarPropriedade(String estabelecimentoId, String comercianteId) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado com ID: " + estabelecimentoId));

        if (!estabelecimento.getComerciante().getId().equals(comercianteId)) {
            throw new AcessoNaoAutorizadoException("O estabelecimento não pertence ao comerciante autenticado.");
        }
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

    public List<EstabelecimentoRespostaDTO> listarPorComerciante(String comercianteId) {
        List<Estabelecimento> estabelecimentos = estabelecimentoRepository.findByComercianteId(comercianteId);

        return estabelecimentos.stream()
                .map(e -> new EstabelecimentoRespostaDTO(
                        e.getId(),
                        e.getNome(),
                        e.getCnpj(),
                        e.getTelefone(),
                        e.getEndereco().getLogradouro(),
                        e.getEndereco().getNumero(),
                        e.getEndereco().getBairro(),
                        e.getEndereco().getMunicipio()
                ))
                .collect(Collectors.toList());
    }

    public List<EstabelecimentoRespostaDTO> listarTodos() {
        return estabelecimentoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public EstabelecimentoRespostaDTO buscarPorId(String id) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));
        return toDTO(estabelecimento);
    }

    private EstabelecimentoRespostaDTO toDTO(Estabelecimento e) {
        EstabelecimentoRespostaDTO dto = new EstabelecimentoRespostaDTO();

        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setCnpj(e.getCnpj());
        dto.setTelefone(e.getTelefone());
        dto.setImagem(e.getImagem());
        dto.setTipoImagem(e.getTipoImagem());

        if (e.getEndereco() != null) {
            dto.setEnderecoId(e.getEndereco().getId());
            dto.setCep(e.getEndereco().getCep());
            dto.setLogradouro(e.getEndereco().getLogradouro());
            dto.setNumero(e.getEndereco().getNumero());
            dto.setBairro(e.getEndereco().getBairro());
            dto.setMunicipio(e.getEndereco().getMunicipio());
        }

        if (e.getComerciante() != null) {
            dto.setComercianteId(e.getComerciante().getId());
            dto.setComercianteNome(e.getComerciante().getNome());
        }

        return dto;
    }
}
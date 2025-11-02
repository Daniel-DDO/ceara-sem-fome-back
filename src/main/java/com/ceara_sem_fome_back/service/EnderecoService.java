package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.EnderecoCadRequest;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Endereco;
import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnderecoService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private CepGeocodingService cepGeocodingService;

    private Endereco cadastrarEndereco(EnderecoCadRequest request) {
        Endereco endereco = new Endereco();
        endereco.setCep(request.getCep());
        endereco.setLogradouro(request.getLogradouro());
        endereco.setNumero(request.getNumero());
        endereco.setBairro(request.getBairro());
        endereco.setMunicipio(request.getMunicipio());

        if (endereco.getLatitude() == null || endereco.getLongitude() == null) {
            Endereco enderecoComCoords = cepGeocodingService.getEnderecoComCep(endereco.getCep());
            endereco.setLatitude(enderecoComCoords.getLatitude());
            endereco.setLongitude(enderecoComCoords.getLongitude());
        }

        return endereco;
    }

    @Transactional
    public Beneficiario cadastrarEnderecoBenef(String beneficiarioId, EnderecoCadRequest request) {
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RuntimeException("Beneficiário não encontrado"));

        beneficiario.setEndereco(cadastrarEndereco(request));

        return beneficiarioRepository.save(beneficiario);
    }

    @Transactional
    public Estabelecimento cadastrarEnderecoEstab(String estabelecimentoId, EnderecoCadRequest request) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        estabelecimento.setEndereco(cadastrarEndereco(request));

        return estabelecimentoRepository.save(estabelecimento);
    }
}

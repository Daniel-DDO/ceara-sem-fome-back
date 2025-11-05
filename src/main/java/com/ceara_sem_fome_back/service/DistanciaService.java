package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Endereco;
import com.ceara_sem_fome_back.model.Estabelecimento;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.EnderecoRepository;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import com.ceara_sem_fome_back.utils.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DistanciaService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private CepGeocodingService cepGeocodingService;


    @Transactional
    protected Endereco garantirCoordenadas(Endereco endereco) {

        if (endereco != null && endereco.getLatitude() != null && endereco.getLongitude() != null) {
            return endereco;
        }

        if (endereco == null || endereco.getCep() == null) {
            throw new IllegalArgumentException("Não é possível calcular distância. Endereço ou CEP ausentes.");
        }

        // Se as coordenadas estiverem ausentes
        Endereco enderecoComCoordenadas = cepGeocodingService.getEnderecoComCep(endereco.getCep());
        endereco.setLatitude(enderecoComCoordenadas.getLatitude());
        endereco.setLongitude(enderecoComCoordenadas.getLongitude());

        return enderecoRepository.save(endereco);
    }

    @Transactional
    public double calcularDistanciaEntreEntidades(String beneficiarioId, String estabelecimentoId) {

        // Buscar Entidades
        Beneficiario beneficiario = beneficiarioRepository.findById(beneficiarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado."));

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estabelecimento não encontrado."));

        Endereco endBeneficiario = garantirCoordenadas(beneficiario.getEndereco());
        Endereco endEstabelecimento = garantirCoordenadas(estabelecimento.getEndereco());

        // Calcular a Distância
        return GeoUtils.calcularDistanciaHaversine(
                endBeneficiario.getLatitude(),
                endBeneficiario.getLongitude(),
                endEstabelecimento.getLatitude(),
                endEstabelecimento.getLongitude()
        );
    }
}

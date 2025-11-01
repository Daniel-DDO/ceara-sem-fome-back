package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.localizacao.NominatimResponse;
import com.ceara_sem_fome_back.dto.localizacao.ViaCepResponse;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import com.ceara_sem_fome_back.model.Endereco;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Service
public class CepGeocodingService {

    @Autowired
    private NominatimService nominatimService;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";

    // Busca o endereço no ViaCEP e geocodifica usando o Nominatim.
    public Endereco getEnderecoComCep(String cep) {
        ViaCepResponse cepResponse;

        // Consulta ViaCEP
        try {
            cepResponse = restTemplate.getForObject(VIACEP_URL, ViaCepResponse.class, cep);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new RecursoNaoEncontradoException("CEP inválido ou mal formatado: " + cep);
        }

        if (cepResponse == null || cepResponse.erro() != null) {
            throw new RecursoNaoEncontradoException("CEP não encontrado na base de dados: " + cep);
        }


        String enderecoFormatado = String.format("%s, %s, %s, %s, Brasil",
                cepResponse.logradouro(),
                cepResponse.bairro(),
                cepResponse.localidade(),
                cepResponse.uf()
        );

        // Consulta Nominatim
        NominatimResponse nomResponse;
        try {
            nomResponse = nominatimService.geocode(enderecoFormatado)
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new RecursoNaoEncontradoException("Coordenadas não encontradas para o endereço.")
                    );
        } catch (RuntimeException e) {
            throw e;
        }


        Endereco endereco = new Endereco();
        endereco.setCep(cepResponse.cep());
        endereco.setLogradouro(cepResponse.logradouro());
        endereco.setBairro(cepResponse.bairro());
        endereco.setMunicipio(cepResponse.localidade());

        // Mapeamento e conversão das coordenadas para Double
        try {
            endereco.setLatitude(Double.parseDouble(nomResponse.lat()));
            endereco.setLongitude(Double.parseDouble(nomResponse.lon()));
        } catch (NumberFormatException e) {
            System.err.println("Erro ao converter coordenadas para Double: " + e.getMessage());
            throw new RuntimeException("Erro interno no formato das coordenadas da API externa.", e);
        }

        return endereco;
    }
}
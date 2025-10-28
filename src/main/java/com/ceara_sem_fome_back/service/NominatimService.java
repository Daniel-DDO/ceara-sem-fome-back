package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.localizacao.NominatimResponse;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
public class NominatimService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    //Converte um endereço completo para Latitude e Longitude.
    public List<NominatimResponse> geocode(String enderecoCompleto) {

        URI uri = UriComponentsBuilder.fromUriString(NOMINATIM_URL)
                .queryParam("q", enderecoCompleto)
                .queryParam("format", "json")
                .queryParam("limit", 1)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "CearaSemFome/1.0 (CearaSemFome@dominio.com)");

        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, uri);

        try {
            NominatimResponse[] responseArray = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    requestEntity,
                    NominatimResponse[].class
            ).getBody();

            if (responseArray == null || responseArray.length == 0) {
                // se o nominatim retorna um array vazio
                throw new RecursoNaoEncontradoException("Coordenadas não encontradas para o endereço: " + enderecoCompleto);
            }

            return Arrays.asList(responseArray);

        } catch (RecursoNaoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Erro ao comunicar com Nominatim: " + e.getMessage());
            throw new RuntimeException("Falha ao geocodificar o endereço.", e);
        }
    }
}
package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.localizacao.NominatimResponse;
import com.ceara_sem_fome_back.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class NominatimService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public List<NominatimResponse> geocode(String enderecoCompleto) {
        try {
            String enderecoCodificado = URLEncoder.encode(enderecoCompleto, StandardCharsets.UTF_8);

            URI uri = URI.create(String.format(
                    "%s?q=%s&format=json&limit=1",
                    NOMINATIM_URL,
                    enderecoCodificado
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "CearaSemFomeApp/1.0 (https://github.com/Daniel-DDO; contato: ceara2fa@gmail.com)");
            headers.add("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            System.out.println("Chamando Nominatim: " + uri);

            NominatimResponse[] responseArray = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    NominatimResponse[].class
            ).getBody();

            if (responseArray == null || responseArray.length == 0) {
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
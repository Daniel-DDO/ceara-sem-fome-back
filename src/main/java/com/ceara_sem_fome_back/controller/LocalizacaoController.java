package com.ceara_sem_fome_back.controller;

import com.ceara_sem_fome_back.dto.localizacao.DistanciaResponse;
import com.ceara_sem_fome_back.model.Endereco;
import com.ceara_sem_fome_back.service.CepGeocodingService;
import com.ceara_sem_fome_back.service.BeneficiarioService;
import com.ceara_sem_fome_back.service.CepGeocodingService;
import com.ceara_sem_fome_back.service.DistanciaService;
import com.ceara_sem_fome_back.service.EstabelecimentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/localizacao")
public class LocalizacaoController {

    @Autowired
    private DistanciaService distanciaService;

    @Autowired
    private CepGeocodingService cepGeocodingService;

    // Endpoint para calcular a distância entre um Beneficiário e um Estabelecimento.
    @GetMapping("/distancia")
    public DistanciaResponse calcularDistanciaEntreEntidades(
            @RequestParam String beneficiarioId,
            @RequestParam String estabelecimentoId) {

        double distanciaKm = distanciaService.calcularDistanciaEntreEntidades(beneficiarioId, estabelecimentoId);

        return new DistanciaResponse(distanciaKm, "km");
    }
    /**
     * Endpoint de teste para consultar um CEP nas APIs externas (ViaCEP e Nominatim).
     * Retorna o Endereco preenchido, com coordenadas, sem salvar no banco.
     *
     * @param cep O CEP a ser consultado (pode ter ou não o hífen).
     * @return Um objeto Endereco com os dados encontrados.
     */
    @GetMapping("/consultar-cep/{cep}")
    public ResponseEntity<Endereco> consultarCep(@PathVariable String cep) {
        // O CepGeocodingService já trata exceções de CEP não encontrado
        Endereco enderecoEncontrado = cepGeocodingService.getEnderecoComCep(cep);
        return ResponseEntity.ok(enderecoEncontrado);
    }
}
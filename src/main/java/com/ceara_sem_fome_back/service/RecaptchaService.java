package com.ceara_sem_fome_back.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.url}")
    private String recaptchaUrl;

    private static final float SCORE_MINIMO = 0.5f;

    private final RestTemplate restTemplate;

    public RecaptchaService() {
        this.restTemplate = new RestTemplate();
    }

    public boolean validarToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", token);

        try {
            RecaptchaResponse response = restTemplate.postForObject(
                    recaptchaUrl,
                    params,
                    RecaptchaResponse.class
            );

            if (response == null) return false;

            boolean aprovado = response.isSuccess() && response.getScore() >= SCORE_MINIMO;

            if (!aprovado) {
                System.out.println("Bloqueado pelo reCAPTCHA v3. Score: " + response.getScore());
                if (response.getErrorCodes() != null) {
                    System.out.println("Erros: " + response.getErrorCodes());
                }
            }

            return aprovado;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RecaptchaResponse {
        private boolean success;

        private float score;

        private String action;
        private String hostname;

        @JsonProperty("error-codes")
        private List<String> errorCodes;
    }
}
package com.ceara_sem_fome_back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import lombok.Data;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.url}")
    private String recaptchaUrl;

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

            return response != null && response.isSuccess();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Data
    private static class RecaptchaResponse {
        private boolean success;
        private String hostname;
    }
}

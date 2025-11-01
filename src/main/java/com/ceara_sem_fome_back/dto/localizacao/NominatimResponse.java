package com.ceara_sem_fome_back.dto.localizacao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimResponse(
        String lat, // Latitude como String
        String lon, // Longitude como String
        String display_name
) {}
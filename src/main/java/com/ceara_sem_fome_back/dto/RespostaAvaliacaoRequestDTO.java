package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RespostaAvaliacaoRequestDTO {

    @NotBlank(message = "O ID da avaliação é obrigatório.")
    private String avaliacaoId;

    @NotBlank(message = "O texto da resposta é obrigatório.")
    private String resposta; // O texto da resposta do comerciante

}
package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvaliacaoRequestDTO {

    @NotBlank(message = "O ID da compra é obrigatório.")
    private String compraId;

    @NotNull(message = "A quantidade de estrelas é obrigatória.")
    @Min(value = 1, message = "A avaliação mínima é 1 estrela.")
    @Max(value = 5, message = "A avaliação máxima é 5 estrelas.")
    private Integer estrelas;
    //comentário não é obrigatório
    private String comentario;
}

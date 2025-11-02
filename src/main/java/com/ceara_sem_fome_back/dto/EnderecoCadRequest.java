package com.ceara_sem_fome_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnderecoCadRequest {

    @NotBlank(message = "O CEP é obrigatório.")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "Formato do CEP inválido. Use no padrão 12345-678")
    private String cep;

    @NotBlank(message = "O logradouro é obrigatório.")
    private String logradouro;

    @NotBlank(message = "O número é obrigatório.")
    private String numero;

    @NotBlank(message = "O bairro é obrigatório.")
    private String bairro;

    @NotBlank(message = "O município é obrigatório.")
    private String municipio;

}

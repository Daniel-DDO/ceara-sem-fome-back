package com.ceara_sem_fome_back.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class LoginDTO {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String senha;

    //essa classe é pra passar apenas o que precisa, ao invés de passar tudo, passa só o necessário
}

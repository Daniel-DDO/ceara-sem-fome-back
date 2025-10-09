package com.ceara_sem_fome_back.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class LoginDTO {
    private String email;
    private String senha;

    //essa classe é pra passar apenas o que precisa, ao invés de passar tudo, passa só o necessário
}

package com.ceara_sem_fome_back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RedefinirSenhaFinalDTO {
    // A prova de identidade do usuário
    private String token; 
    
    // Senhas do formulário
    private String novaSenha; 
    private String confirmaNovaSenha;
}

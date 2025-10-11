package com.ceara_sem_fome_back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NovaSenhaDTO {
    // Usamos o email para identificar o usuário no banco de dados
    private String email; 
    
    // A nova senha que o usuário escolheu
    private String novaSenha; 
    
    // A repetição da nova senha (para validação do backend)
    private String confirmaNovaSenha;
}

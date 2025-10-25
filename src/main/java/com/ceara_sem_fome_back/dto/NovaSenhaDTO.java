package com.ceara_sem_fome_back.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NovaSenhaDTO {
    //o email é usado para identificar o usuário no banco de dados
    private String email; 
    
    //nova senha que o usuário escolheu
    private String novaSenha; 
    
    //repetição da nova senha
    private String confirmaNovaSenha;
}

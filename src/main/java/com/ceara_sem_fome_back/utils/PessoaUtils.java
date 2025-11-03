package com.ceara_sem_fome_back.utils;

public class PessoaUtils {
    public static boolean verificarCpf(String cpf) {
        //Lógica para remover a máscara do CPF e validar o tamanho
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        
        //Verifica se o CPF tem 11 dígitos
        return cpfLimpo.length() == 11;
    }
}
package com.ceara_sem_fome_back.utils;

public class PessoaUtils {
    // Este é um método de exemplo. Você pode adicionar a sua lógica de validação de CPF aqui.
    public static boolean verificarCpf(String cpf) {
        // Lógica para validar CPF
        return cpf != null && cpf.length() == 11;
    }
}
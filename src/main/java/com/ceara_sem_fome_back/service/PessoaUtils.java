package com.ceara_sem_fome_back.service;

public class PessoaUtils {

    private PessoaUtils() {}

    public static boolean verificarCpf(String cpf) {
        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11) {
            return false;
        }

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int[] numeros = cpf.chars().map(c -> c - '0').toArray();
        int soma1 = 0, soma2 = 0;

        for (int i = 0, peso = 10; i < 9; i++, peso--) {
            soma1 += numeros[i] * peso;
        }
        int digito1 = 11 - (soma1 % 11);
        if (digito1 > 9) digito1 = 0;

        for (int i = 0, peso = 11; i < 10; i++, peso--) {
            soma2 += numeros[i] * peso;
        }
        int digito2 = 11 - (soma2 % 11);
        if (digito2 > 9) digito2 = 0;

        return numeros[9] == digito1 && numeros[10] == digito2;
    }
}

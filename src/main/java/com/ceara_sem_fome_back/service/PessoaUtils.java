package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Pessoa;
import java.time.LocalDate;

public class PessoaUtils {

    private PessoaUtils() {}

    public Pessoa cadastrar(String nome, String cpf, String email, String senha, LocalDate dataNascimento, String telefone, String genero) throws Exception {
        if (nome == null || nome.isEmpty()) {
            throw new Exception("O nome não pode ser vazio.");
        } else if (cpf == null || cpf.isEmpty()) {
            throw new Exception("O cpf não pode ser vazio.");
        } else if (email == null || !email.contains("@")) {
            throw new Exception("O email não pode ser vazio e deve conter @.");
        } else if (senha == null || senha.length() < 8) {
            throw new Exception("A senha não pode ser vazia e nem menor do que 8 caracteres.");
        } else if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new Exception("A data de nascimento não pode ser vazia ou futura.");
        } else if (telefone == null || telefone.isEmpty()) {
            throw new Exception("O telefone não pode ser vazio.");
        } else {
            if (genero == null || genero.isEmpty()) {
                genero = "Personalizado"; //temporário
            }

            //precisa de separação e verificação
            //se é cadastro de beneficiário, comerciante, adm...
            //ent esse metodo vai pra cada classe service, conforme oq formos fazendo

            if (!verificarCpf(cpf)) {
                throw new Exception("Insira um CPF válido.");
            }

            //sobre a duplicidade, precisa do BD pra fazer, mas é bem imediato
            //aqui é pra criar o objeto Pessoa, pela subclasse.

        }
        return null;
    }

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

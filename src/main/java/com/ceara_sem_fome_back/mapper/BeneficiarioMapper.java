package com.ceara_sem_fome_back.mapper;

import com.ceara_sem_fome_back.dto.BeneficiarioRespostaDTO;
import com.ceara_sem_fome_back.dto.EnderecoRespostaDTO;
import com.ceara_sem_fome_back.model.Beneficiario;

public class BeneficiarioMapper {

    public static BeneficiarioRespostaDTO toDTO(Beneficiario b) {
        if (b == null) return null;

        return BeneficiarioRespostaDTO.builder()
                .id(b.getId())
                .nome(b.getNome())
                .cpf(b.getCpf())
                .email(b.getEmail())
                .dataNascimento(b.getDataNascimento())
                .telefone(b.getTelefone())
                .genero(b.getGenero())
                .lgpdAccepted(b.getLgpdAccepted())
                .numeroCadastroSocial(b.getNumeroCadastroSocial())
                .endereco(b.getEndereco() != null ? EnderecoRespostaDTO.builder()
                        .id(b.getEndereco().getId())
                        .cep(b.getEndereco().getCep())
                        .logradouro(b.getEndereco().getLogradouro())
                        .numero(b.getEndereco().getNumero())
                        .bairro(b.getEndereco().getBairro())
                        .municipio(b.getEndereco().getMunicipio())
                        .build() : null)
                .conta(b.getConta())
                .status(b.getStatus())
                .build();
    }
}

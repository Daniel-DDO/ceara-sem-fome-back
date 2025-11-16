package com.ceara_sem_fome_back.mapper;

import com.ceara_sem_fome_back.dto.ComercianteRespostaDTO;
import com.ceara_sem_fome_back.dto.EstabelecimentoResumoResponse;
import com.ceara_sem_fome_back.model.Comerciante;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
public class ComercianteMapper {

    public ComercianteRespostaDTO toDTO(Comerciante comerciante) {
        ComercianteRespostaDTO dto = new ComercianteRespostaDTO();
        dto.setId(comerciante.getId().toString());
        dto.setNome(comerciante.getNome());
        dto.setCpf(comerciante.getCpf());
        dto.setEmail(comerciante.getEmail());
        dto.setDataNascimento(comerciante.getDataNascimento());
        dto.setTelefone(comerciante.getTelefone());
        dto.setGenero(comerciante.getGenero());
        dto.setLgpdAccepted(comerciante.getLgpdAccepted());

        if (comerciante.getEstabelecimentos() != null) {
            dto.setEstabelecimentos(
                    comerciante.getEstabelecimentos()
                            .stream()
                            .map(e -> {
                                EstabelecimentoResumoResponse er = new EstabelecimentoResumoResponse();
                                er.setId(e.getId().toString());
                                er.setNome(e.getNome());
                                er.setCnpj(e.getCnpj());
                                return er;
                            })
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }
}

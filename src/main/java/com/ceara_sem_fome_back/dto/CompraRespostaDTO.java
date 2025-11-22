package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.Avaliacao;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraRespostaDTO {

    private String id;
    private LocalDateTime dataHora;
    private Double valorTotal;
    private String beneficiarioNome;
    private String beneficiarioId;
    private String estabelecimentoNome;
    private String estabelecimentoId;
    private String comercianteNome;
    private String comercianteId;
    private List<CompraItemDTO> itens;


}

package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoEstabDTO {

    private String id;
    private ProdutoDTO produtoDTO;
    private EstabelecimentoRespostaDTO estabelecimentoRespostaDTO;
}

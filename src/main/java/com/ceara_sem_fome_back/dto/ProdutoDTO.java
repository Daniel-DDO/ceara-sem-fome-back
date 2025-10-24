package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.StatusProduto;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ProdutoDTO {
    private String id;
    private String nome;
    private String lote;
    private String descricao;
    private double preco;
    private int quantidadeEstoque;
    private StatusProduto status;
}

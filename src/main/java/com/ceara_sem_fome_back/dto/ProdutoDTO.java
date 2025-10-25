package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.StatusProduto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProdutoDTO {
    private String id;
    private String nome;
    private String lote;
    private String descricao;
    private double preco;
    private int quantidadeEstoque;
    private StatusProduto status;
    private String imagemUrl; //ex.: "/produto/imagem/{id}"

    public ProdutoDTO(String id, String nome, String lote, String descricao, double preco, int quantidadeEstoque, StatusProduto status) {
        this.id = id;
        this.nome = nome;
        this.lote = lote;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.status = status;
    }

}

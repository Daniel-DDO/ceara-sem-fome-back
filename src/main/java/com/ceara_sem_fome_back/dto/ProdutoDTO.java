package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.CategoriaProduto;
import com.ceara_sem_fome_back.model.StatusProduto;
import com.ceara_sem_fome_back.model.UnidadeProduto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoDTO {

    private String id;
    private String nome;
    private String lote;
    private String descricao;
    private BigDecimal preco;
    private int quantidadeEstoque;
    private StatusProduto status;
    private CategoriaProduto categoria;
    private UnidadeProduto unidade;
    private String imagem;
    private String tipoImagem;
    private String comercianteId;

    public ProdutoDTO(String id, String nome, String lote, String descricao, BigDecimal preco, int quantidadeEstoque, StatusProduto status) {
        this.id = id;
        this.nome = nome;
        this.lote = lote;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
        this.status = status;
    }

}





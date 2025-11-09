package com.ceara_sem_fome_back.dto;

public class AdicionarItemCarrinhoRequest {
    private String produtoId;
    private Integer quantidade;

    // getters and stters
    public String getProdutoId() { return produtoId; }
    public void setProdutoId(String produtoId) { this.produtoId = produtoId; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}

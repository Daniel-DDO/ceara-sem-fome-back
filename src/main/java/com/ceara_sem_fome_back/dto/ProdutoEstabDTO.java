package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.CategoriaProduto;
import com.ceara_sem_fome_back.model.Endereco;
import com.ceara_sem_fome_back.model.UnidadeProduto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoEstabDTO {

    private String id;
    private String nomeProduto;
    private String nomeEstabelecimento;

    private BigDecimal preco;
    private Integer quantidadeEstoque;

    private CategoriaProduto categoria;
    private UnidadeProduto unidade;

    private String imagem;
    private String tipoImagem;

    private Endereco endereco;
}

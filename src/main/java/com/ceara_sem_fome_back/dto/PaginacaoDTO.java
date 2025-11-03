package com.ceara_sem_fome_back.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginacaoDTO<T> {
    private List<T> conteudo;
    private int paginaAtual;
    private int totalPaginas;
    private long totalElementos;
    private int tamanhoPagina;
    private boolean ultimaPagina;

    public PaginacaoDTO() {
    }

    public PaginacaoDTO(List<T> conteudo, int paginaAtual, int totalPaginas, long totalElementos, int tamanhoPagina, boolean ultimaPagina) {
        this.conteudo = conteudo;
        this.paginaAtual = paginaAtual;
        this.totalPaginas = totalPaginas;
        this.totalElementos = totalElementos;
        this.tamanhoPagina = tamanhoPagina;
        this.ultimaPagina = ultimaPagina;
    }
}
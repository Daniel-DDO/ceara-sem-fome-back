package com.ceara_sem_fome_back.dto;

import com.ceara_sem_fome_back.model.CategoriaComunicado;
import com.ceara_sem_fome_back.model.Comunicado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComunicadoDTO {
    private String id;
    private String titulo;
    private String mensagem;
    private LocalDateTime dataHora;
    private String administradorNome;
    private String administradorId;
    private CategoriaComunicado categoria;

    public ComunicadoDTO(Comunicado c) {
        this.id = c.getId();
        this.titulo = c.getTitulo();
        this.mensagem = c.getMensagem();
        this.dataHora = c.getDataHora();
        this.administradorNome = c.getAdministrador().getNome();
        this.administradorId = c.getAdministrador().getId();
        this.categoria = c.getCategoria();
    }
}


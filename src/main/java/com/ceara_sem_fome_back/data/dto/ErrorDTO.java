package com.ceara_sem_fome_back.data.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDTO {
    private String message;
    private int status;

    public ErrorDTO(String message, int status) {
        this.message = message;
        this.status = status;
    }

}
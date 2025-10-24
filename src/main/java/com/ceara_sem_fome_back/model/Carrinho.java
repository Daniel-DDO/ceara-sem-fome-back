package com.ceara_sem_fome_back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String status;
    private LocalDateTime criacao;
    private LocalDateTime modificacao;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ProdutoCarrinho> produtos = new ArrayList<>();

}

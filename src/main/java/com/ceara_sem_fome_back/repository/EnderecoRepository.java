package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository   extends JpaRepository<Endereco, Integer> {
}

package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Comunicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComunicadoRepository extends JpaRepository<Comunicado, String> {
    List<Comunicado> findByAtivo(Boolean ativo);
}

package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, String> {
    List<Endereco> findByMunicipio(String municipio);
    List<Endereco> findByBairro(String bairro);
    List<Endereco> findByMunicipioAndBairro(String municipio, String bairro);

}
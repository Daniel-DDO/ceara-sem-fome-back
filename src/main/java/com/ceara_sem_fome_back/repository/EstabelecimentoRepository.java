package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Estabelecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstabelecimentoRepository extends JpaRepository<Estabelecimento, String> {
    Optional<Estabelecimento> findById(String id);

    boolean existsByNomeAndComerciante(String nome, Comerciante comerciante);

    List<Estabelecimento> findByEnderecoMunicipio(String municipio);
    List<Estabelecimento> findByEnderecoBairro(String bairro);
    List<Estabelecimento> findByEnderecoMunicipioAndEnderecoBairro(String municipio, String bairro);


    Page<Estabelecimento> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    List<Estabelecimento> findByComercianteId(String comercianteId);
}

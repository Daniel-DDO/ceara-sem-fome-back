package com.ceara_sem_fome_back.repository;

import com.ceara_sem_fome_back.model.Beneficiario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiarioRepository extends JpaRepository<Beneficiario, String> {
    Optional<Beneficiario> findByEmail(String email);
    Optional<Beneficiario> findByCpf(String cpf);
    Optional<Beneficiario> findByCpfAndEmail(String cpf, String email);

    //MÉTODOS NOVOS E OTIMIZADOS PARA VALIDAÇÃO
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    List<Beneficiario> findByEnderecoBairro(String bairro);
    List<Beneficiario> findByEnderecoMunicipio(String municipio);

    Page<Beneficiario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    // Metodo para buscar um usuário por ID ignorando o filtro @Where
    @Query("SELECT p FROM #{#entityName} p WHERE p.id = :id")
    Optional<Beneficiario> findByIdIgnoringStatus(@Param("id") String id);

    // NOVOS MÉTODOS PARA ESTATÍSTICAS
    long countByEnderecoMunicipioIgnoreCase(String municipio);
    long countByEnderecoBairroIgnoreCase(String bairro);
}
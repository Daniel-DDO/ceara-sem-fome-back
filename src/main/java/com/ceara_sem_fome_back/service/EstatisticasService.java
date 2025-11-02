package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.EstatisticasRegionaisDTO;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.CompraRepository;
import com.ceara_sem_fome_back.repository.EntregadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstatisticasService {

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Transactional(readOnly = true)
    public EstatisticasRegionaisDTO getEstatisticasPorMunicipio(String municipio) {
        
        long totalBeneficiarios = beneficiarioRepository.countByEnderecoMunicipioIgnoreCase(municipio);
        long totalCompras = compraRepository.countByEnderecoMunicipioIgnoreCase(municipio);
        long totalEntregadores = entregadorRepository.countByEnderecoMunicipioIgnoreCase(municipio);

        return new EstatisticasRegionaisDTO(
                municipio,
                "Município",
                totalBeneficiarios,
                totalCompras,
                totalEntregadores
        );
    }

    @Transactional(readOnly = true)
    public EstatisticasRegionaisDTO getEstatisticasPorBairro(String bairro) {

        long totalBeneficiarios = beneficiarioRepository.countByEnderecoBairroIgnoreCase(bairro);
        long totalCompras = compraRepository.countByEnderecoBairroIgnoreCase(bairro);
        long totalEntregadores = entregadorRepository.countByEnderecoBairroIgnoreCase(bairro);

        return new EstatisticasRegionaisDTO(
                bairro,
                "Bairro",
                totalBeneficiarios,
                totalCompras,
                totalEntregadores
        );
    }
}
package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.EnderecoCadRequest;
import com.ceara_sem_fome_back.dto.RegiaoResponse;
import com.ceara_sem_fome_back.model.*;
import com.ceara_sem_fome_back.repository.EnderecoRepository;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
import com.ceara_sem_fome_back.repository.AdministradorRepository;
import com.ceara_sem_fome_back.repository.EntregadorRepository;
import com.ceara_sem_fome_back.repository.EstabelecimentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private ComercianteRepository comercianteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private EntregadorRepository entregadorRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Autowired
    private CepGeocodingService cepGeocodingService;


    // cadastrar endereço genérico
    @Transactional
    public Endereco cadastrarEndereco(EnderecoCadRequest request) {
        Endereco endereco = new Endereco();
        endereco.setCep(request.getCep());
        endereco.setLogradouro(request.getLogradouro());
        endereco.setNumero(request.getNumero());
        endereco.setBairro(request.getBairro());
        endereco.setMunicipio(request.getMunicipio());

        if (request.getLatitude() != null && request.getLongitude() != null) {
            Endereco enderecoComCoords = cepGeocodingService.getEnderecoComCep(request.getCep());
            endereco.setLatitude(enderecoComCoords.getLatitude());
            endereco.setLongitude(enderecoComCoords.getLongitude());
        }else {
            endereco.setLatitude(request.getLatitude());
            endereco.setLongitude(request.getLongitude());
        }
        return enderecoRepository.save(endereco);
    }

    // Cadastro do endereço do Admin
    @Transactional
    public Administrador cadastrarEnderecoAdministrador(String adminId, EnderecoCadRequest request) {
        Administrador administrador = administradorRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Administrador não encontrado"));

        cadastrarEndereco(request);
        return administradorRepository.save(administrador);
    }

    // Cadastro do endereço de Beneficiário
    @Transactional
    public Beneficiario cadastrarEnderecoBeneficiario(String id, EnderecoCadRequest request) {
        Beneficiario beneficiario = beneficiarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado"));

        beneficiario.setEndereco(cadastrarEndereco(request));
        return beneficiarioRepository.save(beneficiario);
    }

    // Cadastro do endereço de Comerciante
    @Transactional
    public Comerciante cadastrarEnderecoComerciante(String id, EnderecoCadRequest request) {
        Comerciante comerciante = comercianteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comerciante não encontrado"));
        cadastrarEndereco(request);
        return comercianteRepository.save(comerciante);
    }

    // Cadastro de endereço de Entregador
    @Transactional
    public Entregador cadastrarEnderecoEntregador(String Id, EnderecoCadRequest request) {
        Entregador entregador = entregadorRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Entregador não encontrado"));

        entregador.setEndereco(cadastrarEndereco(request));
        return entregadorRepository.save(entregador);
    }

    // Cadastrar endereço de um estabelecimento
    @Transactional
    public Estabelecimento cadastrarEnderecoEstabelecimento(String estabelecimentoId, EnderecoCadRequest request) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        estabelecimento.setEndereco(cadastrarEndereco(request));

        return estabelecimentoRepository.save(estabelecimento);
    }

    // Atualizar um endereço existente de beneficiário
    public Endereco atualizarEndereco(String id, Endereco novoEndereco) {
        Endereco existente = enderecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com ID: " + id));

        existente.setCep(novoEndereco.getCep());
        existente.setLogradouro(novoEndereco.getLogradouro());
        existente.setNumero(novoEndereco.getNumero());
        existente.setBairro(novoEndereco.getBairro());
        existente.setMunicipio(novoEndereco.getMunicipio());
        existente.setLatitude(novoEndereco.getLatitude());
        existente.setLongitude(novoEndereco.getLongitude());

        return enderecoRepository.save(existente);
    }

    // Buscar por ID
    public Endereco buscarPorId(String id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com ID: " + id));
    }

    // Buscar por região (bairro / cidade)
    public List<Endereco> listarPorRegiao(String municipio, String bairro) {
        if (municipio != null && bairro != null) {
            return enderecoRepository.findByMunicipioAndBairro(municipio, bairro);
        } else if (municipio != null) {
            return enderecoRepository.findByMunicipio(municipio);
        } else if (bairro != null) {
            return enderecoRepository.findByBairro(bairro);
        } else {
            return enderecoRepository.findAll();
        }
    }

    // método para listar por região e bairro
    @Transactional(readOnly = true)
    public RegiaoResponse listarEntidadesPorRegiao(String municipio, String bairro) {
        List<Beneficiario> beneficiarios;
        List<Estabelecimento> estabelecimentos;

        if (municipio != null && bairro != null) {
            beneficiarios = beneficiarioRepository.findByEnderecoMunicipioAndEnderecoBairro(municipio, bairro);
            estabelecimentos = estabelecimentoRepository.findByEnderecoMunicipioAndEnderecoBairro(municipio, bairro);
        } else if (municipio != null) {
            beneficiarios = beneficiarioRepository.findByEnderecoMunicipio(municipio);
            estabelecimentos = estabelecimentoRepository.findByEnderecoMunicipio(municipio);
        } else if (bairro != null) {
            beneficiarios = beneficiarioRepository.findByEnderecoBairro(bairro);
            estabelecimentos = estabelecimentoRepository.findByEnderecoBairro(bairro);
        } else {
            beneficiarios = beneficiarioRepository.findAll();
            estabelecimentos = estabelecimentoRepository.findAll();
        }

        return new RegiaoResponse(municipio, bairro, beneficiarios, estabelecimentos);
    }

}

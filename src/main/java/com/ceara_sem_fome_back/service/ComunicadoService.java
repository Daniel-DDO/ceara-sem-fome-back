package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.dto.ComunicadoDTO;
import com.ceara_sem_fome_back.model.Administrador;
import com.ceara_sem_fome_back.model.Beneficiario;
import com.ceara_sem_fome_back.model.Comerciante;
import com.ceara_sem_fome_back.model.Comunicado;
import com.ceara_sem_fome_back.repository.BeneficiarioRepository;
import com.ceara_sem_fome_back.repository.ComercianteRepository;
import com.ceara_sem_fome_back.repository.ComunicadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ComunicadoService {

    @Autowired
    private ComunicadoRepository comunicadoRepository;

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository; // Injetar

    @Autowired
    private ComercianteRepository comercianteRepository; // Injetar

    @Autowired
    private NotificacaoService notificacaoService;

    @Transactional
    public ComunicadoDTO criar(ComunicadoDTO dto) {

        Administrador admin = administradorService.buscarAdmPorId(dto.getAdministradorId());
        if (admin == null) {
            throw new RuntimeException("Administrador não encontrado.");
        }

        Comunicado comunicado = new Comunicado();
        comunicado.setAdministrador(admin);
        comunicado.setTitulo(dto.getTitulo());
        comunicado.setMensagem(dto.getMensagem());
        comunicado.setCategoria(dto.getCategoria());
        comunicado.setAtivo(true);

        Comunicado salvo = comunicadoRepository.save(comunicado);

        String msgNotificacao = "Novo Comunicado Oficial: " + salvo.getTitulo();

        List<Beneficiario> beneficiarios = beneficiarioRepository.findAll();
        for (Beneficiario b : beneficiarios) {
            try {
                notificacaoService.criarEEnviarNotificacao(b.getId(), msgNotificacao);
            } catch (Exception e) {
                System.err.println("Erro ao notificar beneficiario " + b.getId());
            }
        }

        List<Comerciante> comerciantes = comercianteRepository.findAll();
        for (Comerciante c : comerciantes) {
            try {
                notificacaoService.criarEEnviarNotificacao(c.getId(), msgNotificacao);
            } catch (Exception e) {
                System.err.println("Erro ao notificar comerciante " + c.getId());
            }
        }

        return new ComunicadoDTO(salvo);
    }

    public List<Comunicado> listarTodos() {
        return comunicadoRepository.findAll();
    }

    public Optional<Comunicado> buscarPorId(String id) {
        return comunicadoRepository.findById(id);
    }

    public void deletar(String id) {
        comunicadoRepository.deleteById(id);
    }

    public Comunicado desativarPorId(String id) {
        Comunicado comunicado = comunicadoRepository.findById(id).orElseThrow();
        comunicado.setAtivo(false);
        comunicadoRepository.save(comunicado);
        return comunicado;
    }

    public Comunicado atualizar(Comunicado comunicado) {
        return comunicadoRepository.save(comunicado);
    }

    public List<Comunicado> listarAtivos() {
        return comunicadoRepository.findByAtivo(true);
    }
}


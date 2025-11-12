package com.ceara_sem_fome_back.service;

import com.ceara_sem_fome_back.model.Compra;
import com.ceara_sem_fome_back.model.EventoCompra;
import com.ceara_sem_fome_back.model.StatusCompra;
import com.ceara_sem_fome_back.repository.CompraRepository;
import com.ceara_sem_fome_back.repository.EventoCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.EnumSet;

import java.util.List;

@Service
public class EventoCompraService {

    @Autowired
    private EventoCompraRepository eventoCompraRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Transactional
    public EventoCompra criarEvento(Compra compra, StatusCompra novoStatus, String descricao) {
        
        // Valida a transicao
        if (!isTransicaoValida(compra.getStatus(), novoStatus)) {
            throw new IllegalStateException("Transicao de status de " + 
                compra.getStatus() + " para " + novoStatus + " nao permitida.");
        }

        //Atualiza o status principal da Compra
        compra.setStatus(novoStatus);
        compraRepository.save(compra);

        //Cria o registro do evento
        EventoCompra evento = new EventoCompra(compra, novoStatus, descricao);
        
        return eventoCompraRepository.save(evento);
    }
    private boolean isTransicaoValida(StatusCompra atual, StatusCompra novo) {
        if (atual == null) {
             return novo == StatusCompra.FINALIZADA;
        }

        switch (atual) {
            case FINALIZADA:
                // De finalizada so pode ir para EM_PREPARACAO ou ser CANCELADA
                return novo == StatusCompra.EM_PREPARACAO || novo == StatusCompra.CANCELADA;
            case EM_PREPARACAO:
                // De em preparacao so pode ir para PRONTO_PARA_ENTREGA ou CANCELADA
                return novo == StatusCompra.PRONTO_PARA_ENTREGA || novo == StatusCompra.CANCELADA;
            case PRONTO_PARA_ENTREGA:
                // De pronto so pode ir para A_CAMINHO (pelo entregador)
                return novo == StatusCompra.A_CAMINHO;
            case A_CAMINHO:
                // De a caminho so pode ir para ENTREGUE
                return novo == StatusCompra.ENTREGUE;
            case ENTREGUE:
            case CANCELADA:
                // De entregue ou cancelada, nao pode ir para nenhum outro estado
                return false; 
            default:
                return false;
        }
    }

    @Transactional(readOnly = true)
    public List<EventoCompra> listarEventosPorCompra(String compraId) {
        if (!compraRepository.existsById(compraId)) {
            //pode usar excecao customizada
            throw new RuntimeException("Compra nao encontrada: " + compraId); 
        }
        return eventoCompraRepository.findByCompraIdOrderByDataHoraEventoAsc(compraId);
    }
}
package com.ceara_sem_fome_back.model;

public enum StatusCompra {
    FINALIZADA, // Pedido pago e recebido pelo comerciante
    EM_PREPARACAO, // Comerciante esta preparando o pedido
    PRONTO_PARA_ENTREGA, // Pedido pronto para retirada pelo entregador
    A_CAMINHO, // Entregador esta a caminho
    ENTREGUE, // Beneficiario recebeu o pedido
    CANCELADA
}
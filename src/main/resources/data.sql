INSERT INTO endereco (id, cep, logradouro, numero, bairro, municipio, latitude, longitude) VALUES
    ('end-1', '60025-061', 'Rua Major Facundo', '100', 'Centro', 'Fortaleza', -3.72750000, -38.52740000),
    ('end-2', '60140-160', 'Avenida Santos Dumont', '2000', 'Aldeota', 'Fortaleza', -3.73340000, -38.49110000),
    ('end-3', '60165-121', 'Rua Tibúrcio Cavalcante', '900', 'Meireles', 'Fortaleza', -3.73020000, -38.49790000),
    ('end-4', '60175-020', 'Avenida Abolição', '4000', 'Mucuripe', 'Fortaleza', -3.71840000, -38.48710000),
    ('end-5', '60020-181', 'Rua Padre Francisco Pinto', '500', 'Benfica', 'Fortaleza', -3.74830000, -38.53690000)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO carrinho (id, status, criacao, modificacao, subtotal) VALUES
    ('car-1', 'ABERTO',      '2025-01-10 10:00:00', '2025-01-10 10:00:00',  0.00),
    ('car-2', 'ABERTO',      '2025-01-11 09:30:00', '2025-01-11 09:30:00',  0.00),
    ('car-3', 'FINALIZADO',  '2025-01-05 12:00:00', '2025-01-05 12:45:00',  0.00)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO conta (id, numero_conta, agencia, saldo, criado_em, atualizado_em, ativa) VALUES
    ('cont-1', '12345-6', '0001', 180.00, '2025-01-01', '2025-01-20', TRUE),
    ('cont-2', '54321-9', '0001', 250.00, '2025-01-02', '2025-02-01', TRUE),
    ('cont-3', '77788-1', '0001', 900.00, '2025-01-03', '2025-02-03', TRUE)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO administrador (id, nome, cpf, email, senha, data_nascimento, telefone, genero, status, lgpd_accepted)
VALUES
    ('adm-1', 'João Almeida', '66230022001', 'joao.admin@csf.gov.br', '123456', '1980-05-10',
     '(85) 90000-0001', 'MASCULINO', 'ATIVO', TRUE)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO comerciante (id, nome, cpf, email, senha, data_nascimento, telefone, genero, status, conta_id, media_avaliacoes, lgpd_accepted)
VALUES
    ('com-1', 'Ana Costa', '56007707075', 'ana.mercearia@gmail.com', '123456', '1988-09-10',
     '(85) 93333-3333', 'FEMININO', 'ATIVO', 'cont-3', NULL, TRUE)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO beneficiario (
    id, nome, cpf, email, senha, data_nascimento, telefone, genero, status,
    numero_cadastro_social, carrinho_id, endereco_id, conta_id, lgpd_accepted
) VALUES
      ('ben-1', 'Maria Souza',   '36782694000', 'maria.souza@gmail.com',   '123456', '1995-07-21',
       '(85) 91111-1111', 'FEMININO', 'ATIVO', 'NCS-1001', 'car-1', 'end-2', 'cont-1', TRUE),

      ('ben-2', 'Carlos Pereira','11499696019', 'carlos.pereira@gmail.com', '123456', '1990-04-10',
       '(85) 92222-2222', 'MASCULINO', 'ATIVO', 'NCS-1002', 'car-2', 'end-3', 'cont-2', TRUE)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO entregador (id, nome, cpf, email, senha, data_nascimento, telefone, genero, status, endereco_id, lgpd_accepted)
VALUES
    ('ent-1', 'Pedro Santos', '01394251017', 'pedro.entregas@gmail.com', '123456',
     '1992-01-05', '(85) 94444-4444', 'MASCULINO', 'ATIVO', 'end-4', TRUE)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO estabelecimento (id, nome, cnpj, telefone, imagem, tipo_imagem, data_cadastro, endereco_id, comerciante_id, media_avaliacoes)
VALUES
    ('est-1', 'Mercearia da Ana', '12345678000101', '(85) 98888-0000',
     NULL, NULL, '2025-01-20 09:00:00', 'end-5', 'com-1', NULL)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO produto (
    id, nome, lote, descricao, preco, quantidade_estoque, status,
    imagem, tipo_imagem, categoria, unidade, data_cadastro,
    avaliado_por_id, data_avaliacao, comerciante_id, media_avaliacoes
) VALUES
      ('prod-1', 'Arroz 5kg',      'L001', 'Arroz branco tipo 1',     25.90, 50, 'AUTORIZADO',
       NULL, NULL, 'OUTROS', 'UNIDADE', '2025-01-20 09:00:00', 'adm-1', '2025-01-21 10:00:00', 'com-1', NULL),
      ('prod-2', 'Feijão 1kg',     'L002', 'Feijão carioca selecionado', 8.50, 40, 'AUTORIZADO',
       NULL, NULL, 'OUTROS', 'UNIDADE', '2025-01-22 11:00:00', 'adm-1', '2025-01-22 12:00:00', 'com-1', NULL),
      ('prod-3', 'Macarrão 500g',  'L003', 'Macarrão espaguete',      6.00, 60, 'AUTORIZADO',
       NULL, NULL, 'OUTROS', 'UNIDADE', '2025-01-23 09:00:00', 'adm-1', '2025-01-24 09:00:00', 'com-1', NULL)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO produto_estabelecimento (id, produto_id, estabelecimento_id) VALUES
    ('pe-1', 'prod-1', 'est-1'),
    ('pe-2', 'prod-2', 'est-1'),
    ('pe-3', 'prod-3', 'est-1')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO produto_carrinho (id, carrinho_id, produto_estabelecimento_id, quantidade) VALUES
    ('pc-1', 'car-1', 'pe-1', 1),
    ('pc-2', 'car-1', 'pe-2', 1),
    ('pc-3', 'car-2', 'pe-3', 2)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO compra (id, data_hora_compra, valor_total, beneficiario_id, status)
VALUES
    ('comp-1', '2025-02-01 14:30:00', 34.40, 'ben-1', 'FINALIZADA'),
    ('comp-2', '2025-02-05 10:20:00', 12.00, 'ben-2', 'FINALIZADA')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO produto_compra (id, compra_id, produto_estabelecimento_id, quantidade, preco_unitario)
VALUES
    ('pc-1', 'comp-1', 'pe-1', 1, 25.90),
    ('pc-2', 'comp-1', 'pe-2', 1, 8.50),
    ('pc-3', 'comp-2', 'pe-3', 2, 6.00)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO avaliacao (id, compra_id, estrelas, comentario, data_avaliacao, resposta_comerciante, data_resposta)
VALUES
    ('av-1', 'comp-1', 5, 'Produtos muito bons!', '2025-02-02 09:00:00',
     'Agradecemos a avaliação!', '2025-02-02 11:00:00'),

    ('av-2', 'comp-2', 4, 'Entrega rápida e preço justo.', '2025-02-06 12:00:00',
     NULL, NULL)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO verification_token (token, user_email, expiry_date, nome, cpf, senha_criptografada,
                                data_nascimento, telefone, genero, tipo_pessoa, lgpd_accepted)
VALUES
    ('token-1', 'dummy@email.com', '2025-12-31 23:59:59',
     'Usuário Teste', '00000000000', 'senha_fake',
     '2000-01-01', '(85) 90000-0000', 'OUTRO', 'TESTE', TRUE)
    ON CONFLICT (token) DO NOTHING;

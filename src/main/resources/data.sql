-- DADOS INICIAIS PARA ADMINISTRADOR

INSERT INTO administrador (id, nome, email, senha, cpf, telefone, genero, data_nascimento, status)
VALUES
    ('adm-001', 'João Administrador', 'admin@teste.com', '123456', '12345678900', '(85)99999-0000', 'Masculino', '1985-01-15', 'ATIVO'),
    ('adm-002', 'Maria Admin', 'maria@teste.com', '654321', '98765432100', '(85)98888-1111', 'Feminino', '1990-05-20', 'ATIVO')
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA COMERCIANTE

INSERT INTO comerciante (id, nome, email, senha, cpf, telefone, genero, data_nascimento, status)
VALUES
    ('com-001', 'Pedro Comerciante', 'pedro@teste.com', '123', '99988877766', '(85)95555-4444', 'Masculino', '1980-09-05', 'ATIVO')
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA BENEFICIARIO

INSERT INTO beneficiario (id, nome, email, senha, cpf, telefone, genero, data_nascimento, numero_cadastro_social, status)
VALUES
    ('ben-001', 'Carlos Beneficiário', 'carlos@teste.com', '123456', '11122233344', '(85)97777-2222', 'Masculino', '1995-07-10', 'SOCIAL-001', 'ATIVO'),
    ('ben-002', 'Ana Beneficiária', 'ana@teste.com', 'abcdef', '55566677788', '(85)96666-3333', 'Feminino', '1998-03-25', 'SOCIAL-002', 'ATIVO')
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA ENTREGADOR

INSERT INTO entregador (id, nome, email, senha, cpf, telefone, genero, data_nascimento, status)
VALUES
    ('ent-001', 'Lucas Entregador', 'lucas@teste.com', 'abc123', '44433322211', '(85)94444-5555', 'Masculino', '1992-11-30', 'ATIVO')
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA ESTABELECIMENTO

INSERT INTO estabelecimento (id, nome, endereco, comerciante_id)
VALUES
    ('est-001', 'Mercadinho Central', 'Rua Principal, 100', 'com-001'),
    ('est-002', 'Padaria do Bairro', 'Av. Secundária, 50', 'com-001')
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA PRODUTO

INSERT INTO produto (id, nome, descricao, preco_base, criador_id, status)
VALUES
    ('prod-001', 'Arroz 5kg', 'Arroz branco tipo 1', 25.50, 'com-001', 'AUTORIZADO'),
    ('prod-002', 'Feijão 1kg', 'Feijão carioca', 8.75, 'com-001', 'AUTORIZADO')
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA PRODUTO_ESTABELECIMENTO

INSERT INTO produto_estabelecimento (produto_id, estabelecimento_id, preco_venda, estoque)
VALUES
    ('prod-001', 'est-001', 25.50, 100),
    ('prod-002', 'est-001', 8.75, 200),
    ('prod-001', 'est-002', 26.00, 50),
    ('prod-002', 'est-002', 9.00, 75)
    ON CONFLICT (produto_id, estabelecimento_id) DO NOTHING;


-- DADOS INICIAIS PARA CARRINHO

INSERT INTO carrinho (id, beneficiario_id, status, criacao, modificacao)
VALUES
    ('car-001', 'ben-001', 'ABERTO', NOW(), NOW()),
    ('car-002', 'ben-002', 'ABERTO', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA CONTA

INSERT INTO conta (id, numero_conta, agencia, saldo, beneficiario_id)
VALUES
    ('cont-001', '12345-6', '0001', 1000.00, 'ben-001'),
    ('cont-002', '65432-1', '0001', 500.00, 'ben-002')
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA ITEM_CARRINHO

INSERT INTO item_carrinho (id, carrinho_id, produto_id, quantidade, preco_unitario)
VALUES
    ('item-001', 'car-001', 'prod-001', 2, 25.50),
    ('item-002', 'car-001', 'prod-002', 3, 8.75)
    ON CONFLICT (id) DO NOTHING;


-- DADOS INICIAIS PARA VERIFICATION_TOKEN

INSERT INTO verification_token (id, token, user_id, expiry_date)
VALUES
    ('tok-001', 'token123abc', 'adm-001', '2025-12-31 23:59:59'),
    ('tok-002', 'token456xyz', 'ben-001', '2025-12-31 23:59:59')
    ON CONFLICT (id) DO NOTHING;

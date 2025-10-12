-- Dados iniciais para ADMINISTRADOR
INSERT INTO administrador (id, nome, email, senha, cpf, telefone, genero, data_nascimento)
VALUES 
('adm-001', 'João Administrador', 'admin@teste.com', '123456', '12345678900', '(85)99999-0000', 'Masculino', '1985-01-15'),
('adm-002', 'Maria Admin', 'maria@teste.com', '654321', '98765432100', '(85)98888-1111', 'Feminino', '1990-05-20')
ON CONFLICT (id) DO NOTHING;

-- Dados iniciais para BENEFICIARIO
INSERT INTO beneficiario (id, nome, email, senha, cpf, telefone, genero, data_nascimento)
VALUES 
('ben-001', 'Carlos Beneficiário', 'carlos@teste.com', '123456', '11122233344', '(85)97777-2222', 'Masculino', '1995-07-10'),
('ben-002', 'Ana Beneficiária', 'ana@teste.com', 'abcdef', '55566677788', '(85)96666-3333', 'Feminino', '1998-03-25')
ON CONFLICT (id) DO NOTHING;

-- Dados iniciais para COMERCIANTE
INSERT INTO comerciante (id, nome, email, senha, cpf, telefone, genero, data_nascimento)
VALUES 
('com-001', 'Pedro Comerciante', 'pedro@teste.com', '123', '99988877766', '(85)95555-4444', 'Masculino', '1980-09-05')
ON CONFLICT (id) DO NOTHING;

-- Dados iniciais para ENTREGADOR
INSERT INTO entregador (id, nome, email, senha, cpf, telefone, genero, data_nascimento)
VALUES 
('ent-001', 'Lucas Entregador', 'lucas@teste.com', 'abc123', '44433322211', '(85)94444-5555', 'Masculino', '1992-11-30')
ON CONFLICT (id) DO NOTHING;

-- Dados iniciais para ESTABELECIMENTO
INSERT INTO estabelecimento (id, nome)
VALUES 
('est-001', 'Mercadinho Central'),
('est-002', 'Padaria do Bairro')
ON CONFLICT (id) DO NOTHING;

-- Dados iniciais para VERIFICATION_TOKEN
INSERT INTO verification_token (id, token, user_email, expiry_date)
VALUES 
(1, 'token123abc', 'admin@teste.com', '2025-12-31 23:59:59'),
(2, 'token456xyz', 'carlos@teste.com', '2025-12-31 23:59:59')
ON CONFLICT (id) DO NOTHING;

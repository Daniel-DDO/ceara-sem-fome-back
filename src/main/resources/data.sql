INSERT INTO endereco (id, cep, logradouro, numero, bairro, municipio, latitude, longitude) VALUES
    ('end-1', '60025-061', 'Rua Major Facundo', '100', 'Centro', 'Fortaleza', -3.7275, -38.5274),
    ('end-2', '60140-160', 'Avenida Santos Dumont', '2000', 'Aldeota', 'Fortaleza', -3.7334, -38.4911),
    ('end-3', '60165-121', 'Rua Tibúrcio Cavalcante', '900', 'Meireles', 'Fortaleza', -3.7302, -38.4979),
    ('end-4', '60175-020', 'Avenida Abolição', '4000', 'Mucuripe', 'Fortaleza', -3.7184, -38.4871),
    ('end-5', '60020-181', 'Rua Padre Francisco Pinto', '500', 'Benfica', 'Fortaleza', -3.7483, -38.5369),
    ('end-6', '61900-410', 'Avenida Dom Almeida Lustosa', '1200', 'Parangaba', 'Maracanaú', -3.8765, -38.6279),
    ('end-7', '61760-000', 'Rua Coronel João Cândido', '55', 'Centro', 'Eusébio', -3.8921, -38.4550),
    ('end-8', '61600-180', 'Rua Coronel Correia', '250', 'Centro', 'Caucaia', -3.7343, -38.6537),
    ('end-9', '61800-000', 'Avenida Domingos Olímpio', '300', 'Centro', 'Pacatuba', -3.9854, -38.6203),
    ('end-10', '61880-000', 'Rua José de Alencar', '80', 'Centro', 'Guaiúba', -4.0403, -38.6375)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO carrinho (id, status, criacao, modificacao)
VALUES
    ('car-1', 'ATIVO', '2025-01-10', '2025-01-10'),
    ('car-2', 'ATIVO', '2025-01-11', '2025-01-11')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO administrador (id, nome, cpf, email, senha, data_nascimento, telefone, genero, status, lgpd_accepted)
VALUES ('adm-1', 'João Almeida', '66230022001', 'joao.admin@csf.gov.br', '123456', '1980-05-10', '(85) 90000-0001', 'MASCULINO', 'ATIVO', true)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO beneficiario (id, nome, cpf, email, senha, data_nascimento, telefone, genero, status, carrinho_id, endereco_id, lgpd_accepted)
VALUES
    ('ben-1', 'Maria Souza', '36782694000', 'maria.souza@gmail.com', '123456', '1995-07-21', '(85) 91111-1111', 'FEMININO', 'ATIVO', 'car-1', 'end-2', true),
    ('ben-2', 'Carlos Pereira', '11499696019', 'carlos.pereira@gmail.com', '123456', '1990-04-10', '(85) 92222-2222', 'MASCULINO', 'ATIVO', 'car-2', 'end-3', true)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO comerciante (id, nome, cpf, email, senha, data_nascimento, telefone, genero, status, lgpd_accepted)
VALUES ('com-1', 'Ana Costa', '56007707075', 'ana.mercearia@gmail.com', '123456', '1988-09-10', '(85) 93333-3333', 'FEMININO', 'ATIVO', true)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO entregador (id, nome, cpf, email, senha, data_nascimento, telefone, genero, status, endereco_id, lgpd_accepted)
VALUES ('ent-1', 'Pedro Santos', '01394251017', 'pedro.entregas@gmail.com', '123456', '1992-01-05', '(85) 94444-4444', 'MASCULINO', 'ATIVO', 'end-4', true)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO conta (id, numero_conta, agencia, saldo, beneficiario_id, comerciante_id)
VALUES
    ('cont-1', '12345-6', '0001', 250.00, 'ben-1', null),
    ('cont-2', '78910-1', '0001', 125.50, 'ben-2', null),
    ('cont-3', '12332-1', '0001', 300.00, null, 'com-1')

    ON CONFLICT (id) DO NOTHING;

INSERT INTO estabelecimento (id, nome, cnpj, telefone, imagem, tipo_imagem, data_cadastro, endereco_id, comerciante_id)
VALUES ('est-1', 'Mercearia da Ana', '9382', '(81) 986092487', null, null, '2025-01-20', 'end-5', 'com-1')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO produto (id, nome, lote, descricao, preco, quantidade_estoque, status, imagem, tipo_imagem, comerciante_id)
VALUES
    ('prod-1', 'Arroz 5kg', 'L001', 'Arroz branco tipo 1', 25.90, 50, 'AUTORIZADO', NULL, NULL, 'com-1'),
    ('prod-2', 'Feijão 1kg', 'L002', 'Feijão carioca', 8.50, 40, 'AUTORIZADO', NULL, NULL, 'com-1'),
    ('prod-3', 'Macarrão 500g', 'L003', 'Macarrão espaguete', 6.00, 60, 'AUTORIZADO', NULL, NULL, 'com-1')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO produto_estabelecimento (id, produto_id, estabelecimento_id) VALUES
    ('pe-1', 'prod-1', 'est-1'),
    ('pe-2', 'prod-2', 'est-1'),
    ('pe-3', 'prod-3', 'est-1')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO produto_carrinho (id, carrinho_id, produto_id, quantidade) VALUES
    ('pc-1', 'car-1', 'prod-1', 1),
    ('pc-2', 'car-2', 'prod-3', 2)
    ON CONFLICT (id) DO NOTHING;

INSERT INTO verification_token (token, user_email, expiry_date, nome, cpf, senha_criptografada, data_nascimento, telefone, genero, tipo_pessoa, lgpd_accepted)
VALUES ('token-1', 'dummy@email.com', '2025-12-31', 'Dummy', '00000000000', '123456', '2000-01-01', '(85) 90000-0000', 'OUTRO', 'TESTE', true)
    ON CONFLICT (token) DO NOTHING;

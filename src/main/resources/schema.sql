/* CearaSemFome-3*/

CREATE TABLE IF NOT EXISTS endereco (
    id VARCHAR(255) PRIMARY KEY,
    cep VARCHAR(30),
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    bairro VARCHAR(255),
    municipio VARCHAR(255),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
    );

CREATE TABLE IF NOT EXISTS carrinho (
    id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(50) NOT NULL,
    criacao TIMESTAMP NOT NULL,
    modificacao TIMESTAMP,
    subtotal NUMERIC(10, 2) NOT NULL DEFAULT 0.00
    );

CREATE TABLE IF NOT EXISTS administrador (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    telefone VARCHAR(50),
    genero VARCHAR(50),
    status VARCHAR(50),
    lgpd_accepted BOOLEAN NOT NULL DEFAULT FALSE
    );

CREATE TABLE IF NOT EXISTS comerciante (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    telefone VARCHAR(50),
    genero VARCHAR(50),
    status VARCHAR(50),
    lgpd_accepted BOOLEAN NOT NULL DEFAULT FALSE
    );

CREATE TABLE IF NOT EXISTS beneficiario (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    telefone VARCHAR(50),
    genero VARCHAR(50),
    status VARCHAR(50),
    numero_cadastro_social VARCHAR(255),
    carrinho_id VARCHAR(255),
    endereco_id VARCHAR(255),
    lgpd_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (endereco_id) REFERENCES endereco (id),
    FOREIGN KEY (carrinho_id) REFERENCES carrinho (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS entregador (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    telefone VARCHAR(50),
    genero VARCHAR(50),
    status VARCHAR(50),
    endereco_id VARCHAR(255),
    lgpd_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (endereco_id) REFERENCES endereco (id)
    );

CREATE TABLE IF NOT EXISTS estabelecimento (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(50) UNIQUE,
    telefone VARCHAR(50),
    imagem TEXT,
    tipo_imagem VARCHAR(100),
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    endereco_id VARCHAR(255),
    comerciante_id VARCHAR(255),
    FOREIGN KEY (endereco_id) REFERENCES endereco (id),
    FOREIGN KEY (comerciante_id) REFERENCES comerciante (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS produto (
    id VARCHAR(255) PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    lote VARCHAR(255),
    descricao TEXT,
    preco NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    quantidade_estoque INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50),
    imagem TEXT,
    tipo_imagem VARCHAR(100),
    comerciante_id VARCHAR(255),
    FOREIGN KEY (comerciante_id) REFERENCES comerciante (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS conta (
    id VARCHAR(255) PRIMARY KEY,
    numero_conta VARCHAR(50),
    agencia VARCHAR(50),
    saldo NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    beneficiario_id VARCHAR(255),
    comerciante_id VARCHAR(255),
    FOREIGN KEY (beneficiario_id) REFERENCES beneficiario (id),
    FOREIGN KEY (comerciante_id) REFERENCES comerciante (id)
    );

CREATE TABLE IF NOT EXISTS produto_carrinho (
    id VARCHAR(255) PRIMARY KEY,
    carrinho_id VARCHAR(255) NOT NULL,
    produto_id VARCHAR(255) NOT NULL,
    quantidade INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (carrinho_id) REFERENCES carrinho (id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE,
    CONSTRAINT unique_produto_por_carrinho UNIQUE (carrinho_id, produto_id)
    );

CREATE TABLE IF NOT EXISTS produto_estabelecimento (
    id VARCHAR(255) PRIMARY KEY,
    produto_id VARCHAR(255) NOT NULL,
    estabelecimento_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE,
    FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimento (id) ON DELETE CASCADE,
    CONSTRAINT unique_produto_por_estabelecimento UNIQUE (produto_id, estabelecimento_id)
    );

CREATE TABLE IF NOT EXISTS verification_token (
    token VARCHAR(255) PRIMARY KEY,
    user_email VARCHAR(255),
    expiry_date TIMESTAMP,
    nome VARCHAR(255),
    cpf VARCHAR(20),
    senha_criptografada VARCHAR(255),
    data_nascimento DATE,
    telefone VARCHAR(50),
    genero VARCHAR(50),
    tipo_pessoa VARCHAR(50),
    lgpd_accepted BOOLEAN
    );

CREATE TABLE IF NOT EXISTS compra (
    id VARCHAR(255) PRIMARY KEY,
    data_hora_compra TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_total NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    beneficiario_id VARCHAR(255) NOT NULL,
    estabelecimento_id VARCHAR(255) NOT NULL,
    endereco_id VARCHAR(255),
    FOREIGN KEY (beneficiario_id) REFERENCES beneficiario (id),
    FOREIGN KEY (estabelecimento_id) REFERENCES estabelecimento (id),
    FOREIGN KEY (endereco_id) REFERENCES endereco (id)
    );

CREATE TABLE IF NOT EXISTS item_compra (
    id VARCHAR(255) PRIMARY KEY,
    compra_id VARCHAR(255) NOT NULL,
    produto_id VARCHAR(255) NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    FOREIGN KEY (compra_id) REFERENCES compra (id) ON DELETE CASCADE,
    FOREIGN KEY (produto_id) REFERENCES produto (id)
    );

CREATE TABLE IF NOT EXISTS evento_compra (
    id VARCHAR(255) PRIMARY KEY,
    compra_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_hora_evento TIMESTAMP NOT NULL,
    descricao VARCHAR(255),
    FOREIGN KEY (compra_id) REFERENCES compra (id) ON DELETE CASCADE
    );
